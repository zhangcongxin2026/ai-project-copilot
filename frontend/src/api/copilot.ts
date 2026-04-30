import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000,
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
}

export const copilotApi = {
  async runWorkflow(input: string): Promise<WorkflowResult> {
    const response = await api.post<WorkflowResult>('/workflow/sync', { input })
    return response.data
  },

  async getWorkflowStatus(id: string): Promise<WorkflowResult> {
    const response = await api.get<WorkflowResult>(`/workflow/${id}`)
    return response.data
  },

  async healthCheck(): Promise<{ status: string }> {
    const response = await api.get('/health')
    return response.data
  },
}
