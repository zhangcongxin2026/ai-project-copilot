import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 120000,
})

export interface WorkflowRequest {
  input: string
}

export interface WorkflowResult {
  id: string
  input: string
  status: string
  stepResults: Array<{
    stepName: string
    result: {
      success: boolean
      output: string
      errorMessage?: string
    }
  }>
  errorMessage?: string
  progress: number
  taskCount: number
  logs: string[]
  startedAt: string
}

export interface WorkflowStartResponse {
  workflowId: string
  status: string
}

export const copilotApi = {
  /**
   * 启动异步工作流
   */
  async startWorkflow(input: string): Promise<WorkflowStartResponse> {
    const response = await api.post<WorkflowStartResponse>('/workflow', { input })
    return response.data
  },

  /**
   * 同步执行工作流（适合简单测试）
   */
  async runWorkflow(input: string): Promise<WorkflowResult> {
    const response = await api.post<WorkflowResult>('/workflow/sync', { input })
    return response.data
  },

  /**
   * 获取工作流状态
   */
  async getWorkflowStatus(id: string): Promise<WorkflowResult> {
    const response = await api.get<WorkflowResult>(`/workflow/${id}`)
    return response.data
  },

  /**
   * 健康检查
   */
  async healthCheck(): Promise<{ status: string; service: string }> {
    const response = await api.get('/health')
    return response.data
  },
}
