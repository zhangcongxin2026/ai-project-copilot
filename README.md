# AI Project Copilot

多 Agent 协同的 AI 项目管理 Copilot 系统

## 功能特性

- **多 Agent 协同**: 需求解析、任务分解、代码生成、测试生成
- **Orchestrator 编排**: 统一调度多个 Agent 完成自动化工作流
- **RAG 增强**: 结合向量检索提升上下文理解能力
- **缓存优化**: 语义缓存降低 Token 成本
- **多模型支持**: Anthropic Claude / OpenAI GPT / 本地 Ollama

## 技术栈

- 后端：Java 17 + Spring Boot 3.2
- LLM 框架：LangChain4j
- 向量嵌入：ONNX All-MiniLM-L6-v2
- 前端：Vue 3 + Vite

## 项目结构

```
ai-project-copilot/
├── copilot-core/          # 核心模块
│   ├── llm/              # LLM 路由和多模型支持
│   ├── cache/            # 缓存管理
│   ├── rag/              # RAG 引擎
│   └── model/            # 数据模型
├── copilot-agents/        # Agent 模块
│   ├── agent/            # 各专业 Agent
│   └── orchestrator/     # 任务编排器
└── copilot-api/          # API 模块
    ├── api/              # REST 控制器
    └── config/           # Spring 配置
```

## 快速开始

### 1. 配置环境变量

```bash
# Anthropic API（可选）
export ANTHROPIC_API_KEY=your_key_here

# OpenAI API（可选）
export OPENAI_API_KEY=your_key_here

# Ollama 本地模型（可选）
export OLLAMA_BASE_URL=http://localhost:11434
```

### 2. 构建项目

```bash
mvn clean install
```

### 3. 运行应用

```bash
cd copilot-api
mvn spring-boot:run
```

### 4. API 使用

```bash
# 健康检查
curl http://localhost:8080/api/health

# 同步执行工作流
curl -X POST http://localhost:8080/api/workflow/sync \
  -H "Content-Type: application/json" \
  -d '{"input": "创建一个用户登录功能"}'
```

## API 端点

| 端点 | 方法 | 描述 |
|------|------|------|
| /api/health | GET | 健康检查 |
| /api/workflow | POST | 异步执行工作流 |
| /api/workflow/sync | POST | 同步执行工作流 |
| /api/workflow/{id} | GET | 获取工作流状态 |

## 许可证

MIT License
