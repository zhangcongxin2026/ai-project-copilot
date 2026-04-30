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
        <el-button @click="clearInput">清空</el-button>
      </div>
    </el-card>

    <el-card v-if="workflowResult" style="margin-top: 20px;">
      <h3>执行结果</h3>

      <el-alert
        :title="workflowResult.errorMessage || '执行成功'"
        :type="workflowResult.errorMessage ? 'error' : 'success'"
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-timeline>
        <el-timeline-item
          v-for="(step, index) in workflowResult.stepResults"
          :key="index"
          :timestamp="step.stepName"
          placement="top"
          :type="getResultType(step.result)"
        >
          <el-card>
            <h4>{{ step.stepName }}</h4>
            <pre class="result-content">{{ formatOutput(step.result.output) }}</pre>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { copilotApi, type WorkflowResult } from '@/api/copilot'

const inputText = ref('')
const running = ref(false)
const workflowResult = ref<WorkflowResult | null>(null)

const runWorkflow = async () => {
  if (!inputText.value.trim()) {
    return
  }

  running.value = true
  try {
    workflowResult.value = await copilotApi.runWorkflow(inputText.value)
  } catch (error) {
    console.error('Workflow failed:', error)
    workflowResult.value = {
      id: 'error',
      input: inputText.value,
      status: 'FAILED',
      stepResults: [],
      errorMessage: '请求失败，请检查后端服务是否运行',
    }
  } finally {
    running.value = false
  }
}

const clearInput = () => {
  inputText.value = ''
  workflowResult.value = null
}

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
}
</style>
