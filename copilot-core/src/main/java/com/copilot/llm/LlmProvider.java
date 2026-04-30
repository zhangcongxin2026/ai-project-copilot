package com.copilot.llm;

/**
 * LLM 提供者枚举
 */
public enum LlmProvider {
    ANTHROPIC,    // Claude
    OPENAI,       // GPT
    OLLAMA,       // 本地模型
    AUTO          // 自动选择
}
