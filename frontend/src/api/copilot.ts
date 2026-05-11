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

export interface RagDocument {
  id: string
  content: string
  metadata?: Record<string, string>
}

export interface SearchResult {
  id: string
  content: string
  metadata?: Record<string, string>
  similarity: number
}

export const copilotApi = {
  // ==================== 工作流 ====================

  async startWorkflow(input: string): Promise<WorkflowStartResponse> {
    const response = await api.post<WorkflowStartResponse>('/workflow', { input })
    return response.data
  },

  async runWorkflow(input: string): Promise<WorkflowResult> {
    const response = await api.post<WorkflowResult>('/workflow/sync', { input })
    return response.data
  },

  async getWorkflowStatus(id: string): Promise<WorkflowResult> {
    const response = await api.get<WorkflowResult>(`/workflow/${id}`)
    return response.data
  },

  async listWorkflows(): Promise<WorkflowResult[]> {
    const response = await api.get<WorkflowResult[]>('/workflows')
    return response.data
  },

  async healthCheck(): Promise<{ status: string; service: string }> {
    const response = await api.get('/health')
    return response.data
  },

  // ==================== RAG 知识库 ====================

  async addDocument(doc: RagDocument): Promise<{ id: string; status: string }> {
    const response = await api.post('/rag/documents', doc)
    return response.data
  },

  async searchDocuments(query: string, maxResults = 5): Promise<SearchResult[]> {
    const response = await api.get<SearchResult[]>('/rag/search', {
      params: { query, maxResults },
    })
    return response.data
  },

  async deleteDocument(id: string): Promise<{ id: string; status: string }> {
    const response = await api.delete(`/rag/documents/${id}`)
    return response.data
  },

  async clearDocuments(): Promise<{ status: string }> {
    const response = await api.delete('/rag/documents')
    return response.data
  },
}
