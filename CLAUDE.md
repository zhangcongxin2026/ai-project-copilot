# AI Project Copilot - 项目开发文档

## 项目概述

多 Agent 协同的 AI 项目管理 Copilot 系统，引入 Orchestrator 进行任务编排，结合 RAG 提升上下文理解能力，支持缓存优化降低 Token 成本。

## 技术栈

- **后端**: Java 17 + Spring Boot 3.2.4
- **LLM 框架**: LangChain4j 0.29.0
- **向量嵌入**: ONNX All-MiniLM-L6-v2 (384 维)
- **缓存**: Caffeine
- **前端**: Vue 3 + TypeScript + Vite + Element Plus

## 项目结构

```
ai-project-copilot/
├── copilot-core/           # 核心模块
│   └── src/main/java/com/copilot/
│       ├── llm/           # LLM 路由和多模型支持
│       │   ├── LlmRouter.java
│       │   ├── LlmClient.java
│       │   ├── AnthropicClient.java
│       │   ├── OpenAiClient.java
│       │   └── OllamaClient.java
│       ├── cache/         # 缓存管理
│       │   └── CacheManager.java
│       ├── rag/           # RAG 引擎
│       │   ├── RagEngine.java
│       │   ├── EmbeddingService.java
│       │   └── LocalEmbeddingService.java
│       └── model/         # 数据模型
│           ├── Task.java
│           ├── Requirement.java
│           ├── LlmRequest.java
│           ├── LlmResponse.java
│           └── Message.java
├── copilot-agents/        # Agent 模块
│   └── src/main/java/com/copilot/
│       ├── agent/         # 各专业 Agent
│       │   ├── Agent.java (基类)
│       │   ├── AgentResult.java
│       │   ├── RequirementAgent.java
│       │   ├── TaskAgent.java
│       │   ├── CodeAgent.java
│       │   └── TestAgent.java
│       └── orchestrator/  # 任务编排器
│           └── Orchestrator.java
└── copilot-api/          # API 模块
    └── src/main/
        ├── java/com/copilot/
        │   ├── CopilotApplication.java
        │   ├── api/
        │   │   └── CopilotController.java
        │   └── config/
        │       └── AppConfig.java
        └── resources/
            └── application.yml
```

## 核心功能

### 1. LLM 路由 (LlmRouter)
- 支持 Anthropic Claude / OpenAI GPT / Ollama 本地模型
- 自动故障转移
- 优先级：Anthropic -> OpenAI -> Ollama

### 2. 缓存管理 (CacheManager)
- SHA-256 哈希缓存键
- 5 分钟 TTL
- 最大 1000 条目
- 命中统计

### 3. RAG 引擎 (RagEngine)
- 内存向量存储（简化实现）
- 余弦相似度检索
- 本地 ONNX 嵌入模型

### 4. Agent 系统
- **RequirementAgent**: 需求解析，提取结构化信息
- **TaskAgent**: 任务分解，生成可执行任务列表
- **CodeAgent**: 代码生成
- **TestAgent**: 测试用例生成

### 5. Orchestrator
- 工作流状态管理
- 多 Agent 协同调度
- 异步执行支持

## API 端点

| 端点 | 方法 | 描述 |
|------|------|------|
| /api/health | GET | 健康检查 |
| /api/workflow | POST | 异步执行工作流 |
| /api/workflow/sync | POST | 同步执行工作流 |
| /api/workflow/{id} | GET | 获取工作流状态 |

## 启动说明

### 后端
```bash
# 设置环境变量
export ANTHROPIC_API_KEY=your_key
export OPENAI_API_KEY=your_key  # 可选

# 构建并运行
mvn clean install
cd copilot-api
mvn spring-boot:run
```

### 前端
```bash
cd frontend
npm install
npm run dev
```

## 配置项

```yaml
llm:
  anthropic:
    api-key: ${ANTHROPIC_API_KEY}
    model-name: claude-sonnet-4-20250514
  openai:
    api-key: ${OPENAI_API_KEY}
  ollama:
    base-url: http://localhost:11434
```

## 工作流示例

```
输入："创建一个用户登录功能"
  ↓
RequirementAgent → 结构化需求
  ↓
TaskAgent → 任务列表
  ↓
CodeAgent → 代码生成
  ↓
输出
```
