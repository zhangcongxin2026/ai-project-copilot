<template>
  <div class="history-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>工作流历史</span>
          <el-button type="primary" @click="refreshList" :loading="loading">刷新</el-button>
        </div>
      </template>

      <el-table :data="workflows" style="width: 100%" v-loading="loading" empty-text="暂无工作流记录">
        <el-table-column prop="id" label="工作流 ID" width="200" />
        <el-table-column prop="input" label="需求描述" min-width="300" show-overflow-tooltip />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taskCount" label="任务数" width="100" align="center" />
        <el-table-column prop="progress" label="进度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :status="row.status === 'COMPLETED' ? 'success' : row.status === 'FAILED' ? 'exception' : undefined" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.startedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="工作流详情" width="800px" destroy-on-close>
      <div v-if="selectedWorkflow">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="工作流 ID">{{ selectedWorkflow.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedWorkflow.status)">{{ getStatusText(selectedWorkflow.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务数">{{ selectedWorkflow.taskCount }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(selectedWorkflow.startedAt) }}</el-descriptions-item>
          <el-descriptions-item label="需求描述" :span="2">{{ selectedWorkflow.input }}</el-descriptions-item>
        </el-descriptions>

        <el-alert v-if="selectedWorkflow.errorMessage" :title="selectedWorkflow.errorMessage" type="error" :closable="false" style="margin-bottom: 20px;" />

        <h4>执行步骤</h4>
        <el-timeline v-if="selectedWorkflow.stepResults.length > 0">
          <el-timeline-item
            v-for="(step, index) in selectedWorkflow.stepResults"
            :key="index"
            :timestamp="step.stepName"
            placement="top"
            :type="step.result.success ? 'success' : 'danger'"
            :hollow="true"
          >
            <el-card>
              <div class="step-header">
                <span>{{ step.stepName }}</span>
                <el-tag :type="step.result.success ? 'success' : 'danger'" size="small">
                  {{ step.result.success ? '成功' : '失败' }}
                </el-tag>
              </div>
              <pre class="result-content">{{ formatOutput(step.result.output) }}</pre>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无执行步骤" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { copilotApi, type WorkflowResult } from '@/api/copilot'

const workflows = ref<WorkflowResult[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const selectedWorkflow = ref<WorkflowResult | null>(null)

const refreshList = async () => {
  loading.value = true
  try {
    workflows.value = await copilotApi.listWorkflows()
  } catch (error) {
    console.error('Failed to load workflows:', error)
  } finally {
    loading.value = false
  }
}

const showDetail = (workflow: WorkflowResult) => {
  selectedWorkflow.value = workflow
  detailVisible.value = true
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'COMPLETED': return 'success'
    case 'FAILED': return 'danger'
    case 'PENDING': return 'info'
    default: return 'warning'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '等待中'
    case 'PARSING_REQUIREMENT': return '解析需求'
    case 'GENERATING_TASKS': return '生成任务'
    case 'GENERATING_CODE': return '生成代码'
    case 'REVIEWING_CODE': return '审查代码'
    case 'COMPLETED': return '已完成'
    case 'FAILED': return '失败'
    default: return status
  }
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const formatOutput = (output: string): string => {
  try {
    return JSON.stringify(JSON.parse(output), null, 2)
  } catch {
    return output
  }
}

onMounted(refreshList)
</script>

<style scoped>
.history-view {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.result-content {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
}
</style>
