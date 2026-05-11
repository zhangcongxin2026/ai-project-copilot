<template>
  <div class="workflow-view">
    <el-card>
      <h3>创建工作流</h3>

      <el-input
        v-model="inputText"
        :rows="6"
        type="textarea"
        placeholder="请输入您的需求描述，例如：创建一个用户登录功能，需要支持邮箱注册、密码登录、记住我功能"
        @keyup.enter.ctrl="runWorkflow"
      />

      <div class="actions" style="margin-top: 20px;">
        <el-button type="primary" @click="runWorkflow" :loading="running">
          开始执行
        </el-button>
        <el-button @click="clearInput" :disabled="running">清空</el-button>
      </div>
    </el-card>

    <!-- 执行状态卡片 -->
    <el-card v-if="running || workflowResult" style="margin-top: 20px;">
      <h3>执行状态</h3>

      <!-- 进度条 -->
      <el-progress
        :percentage="progress"
        :status="progressStatus"
        :stroke-width="20"
        style="margin-bottom: 20px;"
      >
        <template #default="{ percentage }">
          <span class="progress-value">{{ percentage }}%</span>
        </template>
      </el-progress>

      <!-- 状态标签 -->
      <div class="status-bar" style="margin-bottom: 20px;">
        <el-tag :type="currentStatusType" size="large">
          <el-icon v-if="running"><Loading /></el-icon>
          {{ statusText }}
        </el-tag>
        <el-tag v-if="taskCount > 0" type="info" style="margin-left: 10px;">
          任务数：{{ taskCount }}
        </el-tag>
        <el-tag v-if="elapsedTime > 0" type="info" style="margin-left: 10px;">
          耗时：{{ elapsedTime }}s
        </el-tag>
      </div>

      <!-- 错误提示 -->
      <el-alert
        v-if="workflowResult?.errorMessage"
        :title="workflowResult.errorMessage"
        type="error"
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <!-- 实时日志 -->
      <el-card v-if="logs.length > 0" style="margin-bottom: 20px;">
        <template #header>
          <div class="card-header">
            <span>执行日志</span>
            <el-button text type="primary" @click="scrollToBottom">滚动到底部</el-button>
          </div>
        </template>
        <div ref="logContainer" class="log-container">
          <div v-for="(log, index) in logs" :key="index" class="log-item">
            {{ log }}
          </div>
        </div>
      </el-card>

      <!-- 步骤结果 -->
      <div v-if="workflowResult?.stepResults?.length > 0">
        <h4>详细结果</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(step, index) in workflowResult.stepResults"
            :key="index"
            :timestamp="step.stepName"
            placement="top"
            :type="getResultType(step.result)"
            :hollow="true"
          >
            <el-card>
              <div class="step-header">
                <h4>{{ step.stepName }}</h4>
                <el-tag :type="step.result.success ? 'success' : 'danger'" size="small">
                  {{ step.result.success ? '成功' : '失败' }}
                </el-tag>
              </div>
              <pre class="result-content">{{ formatOutput(step.result.output) }}</pre>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted, nextTick } from 'vue'
import { copilotApi, type WorkflowResult } from '@/api/copilot'
import { Loading } from '@element-plus/icons-vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

const inputText = ref('')
const running = ref(false)
const workflowResult = ref<WorkflowResult | null>(null)
const logContainer = ref<HTMLElement | null>(null)

// 实时状态
const progress = ref(0)
const taskCount = ref(0)
const logs = ref<string[]>([])
const startTime = ref<number | null>(null)
const elapsedTime = ref(0)
const timer = ref<number | null>(null)
const currentWorkflowId = ref<string | null>(null)
let stompClient: Client | null = null

const statusText = computed(() => {
  if (!workflowResult.value) return '等待开始'
  switch (workflowResult.value.status) {
    case 'PENDING': return '等待中'
    case 'PARSING_REQUIREMENT': return '解析需求中...'
    case 'GENERATING_TASKS': return '生成任务中...'
    case 'GENERATING_CODE': return '生成代码中...'
    case 'REVIEWING_CODE': return '代码审查中...'
    case 'COMPLETED': return '已完成'
    case 'FAILED': return '执行失败'
    default: return '未知状态'
  }
})

const currentStatusType = computed(() => {
  if (!workflowResult.value) return 'info'
  switch (workflowResult.value.status) {
    case 'COMPLETED': return 'success'
    case 'FAILED': return 'danger'
    default: return 'warning'
  }
})

const progressStatus = computed(() => {
  if (workflowResult.value?.status === 'FAILED') return 'exception'
  if (workflowResult.value?.status === 'COMPLETED') return 'success'
  return undefined
})

const startTimer = () => {
  startTime.value = Date.now()
  timer.value = window.setInterval(() => {
    if (startTime.value) {
      elapsedTime.value = Math.floor((Date.now() - startTime.value) / 1000)
    }
  }, 1000)
}

const stopTimer = () => {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
}

const updateStatus = (result: WorkflowResult) => {
  workflowResult.value = result
  progress.value = result.progress || 0
  taskCount.value = result.taskCount || 0
  logs.value = result.logs || []
  nextTick(scrollToBottom)
}

const scrollToBottom = () => {
  if (logContainer.value) {
    logContainer.value.scrollTop = logContainer.value.scrollHeight
  }
}

const connectWebSocket = (workflowId: string) => {
  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    reconnectDelay: 3000,
    onConnect: () => {
      stompClient?.subscribe(`/topic/workflow/${workflowId}`, (message) => {
        try {
          const result: WorkflowResult = JSON.parse(message.body)
          updateStatus(result)
          if (result.status === 'COMPLETED' || result.status === 'FAILED') {
            running.value = false
            stopTimer()
            disconnectWebSocket()
          }
        } catch (e) {
          console.error('Failed to parse WebSocket message:', e)
        }
      })
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame.headers['message'])
    },
  })
  stompClient.activate()
}

const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
}

const runWorkflow = async () => {
  if (!inputText.value.trim()) return

  running.value = true
  progress.value = 0
  logs.value = []
  startTime.value = null
  elapsedTime.value = 0
  workflowResult.value = null
  startTimer()

  try {
    const { workflowId } = await copilotApi.startWorkflow(inputText.value)
    currentWorkflowId.value = workflowId

    // Connect WebSocket for real-time updates
    connectWebSocket(workflowId)

    // Fallback polling in case WebSocket fails
    const fallbackPoll = async () => {
      if (!running.value || !currentWorkflowId.value) return
      try {
        const result = await copilotApi.getWorkflowStatus(workflowId)
        updateStatus(result)
        if (result.status === 'COMPLETED' || result.status === 'FAILED') {
          running.value = false
          stopTimer()
          disconnectWebSocket()
          return
        }
      } catch (e) {
        // ignore poll errors when WS is active
      }
      if (running.value) {
        setTimeout(fallbackPoll, 3000)
      }
    }
    // Start fallback poll after 5s if WS hasn't completed
    setTimeout(fallbackPoll, 5000)
  } catch (error) {
    console.error('Workflow failed:', error)
    running.value = false
    stopTimer()
    workflowResult.value = {
      id: 'error',
      input: inputText.value,
      status: 'FAILED',
      stepResults: [],
      errorMessage: '请求失败，请检查后端服务是否运行',
      progress: 0,
      taskCount: 0,
      logs: [],
      startedAt: new Date().toISOString(),
    }
  }
}

const clearInput = () => {
  inputText.value = ''
  workflowResult.value = null
  progress.value = 0
  taskCount.value = 0
  logs.value = []
  elapsedTime.value = 0
  running.value = false
  currentWorkflowId.value = null
  stopTimer()
  disconnectWebSocket()
}

onUnmounted(() => {
  stopTimer()
  disconnectWebSocket()
})

const getResultType = (result: { success: boolean }) => {
  return result.success ? 'success' : 'danger'
}

const formatOutput = (output: string): string => {
  try {
    return JSON.stringify(JSON.parse(output), null, 2)
  } catch {
    return output
  }
}
</script>

<style scoped>
.workflow-view {
  max-width: 1200px;
  margin: 0 auto;
}

.actions {
  display: flex;
  gap: 10px;
}

.result-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-word;
  margin-top: 10px;
  max-height: 400px;
  overflow-y: auto;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-header h4 {
  margin: 0;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  background: #1e1e1e;
  border-radius: 4px;
  padding: 10px;
}

.log-item {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  color: #d4d4d4;
  padding: 4px 0;
  border-bottom: 1px solid #333;
}

.log-item:last-child {
  border-bottom: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-value {
  font-weight: bold;
  color: #409EFF;
}

.status-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
