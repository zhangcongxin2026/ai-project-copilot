package com.copilot.agent;

import com.copilot.llm.LlmRouter;
import com.copilot.model.LlmRequest;
import com.copilot.model.LlmResponse;
import com.copilot.model.Message;

import java.util.List;

/**
 * Agent 基类
 */
public abstract class Agent {

    protected final LlmRouter llmRouter;
    protected final String name;
    protected final String systemPrompt;

    public Agent(LlmRouter llmRouter, String name, String systemPrompt) {
        this.llmRouter = llmRouter;
        this.name = name;
        this.systemPrompt = systemPrompt;
    }

    /**
     * 执行 Agent 任务
     */
    public abstract AgentResult execute(String input);

    /**
     * 调用 LLM
     */
    protected LlmResponse callLlm(String userPrompt) {
        LlmRequest request = LlmRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .temperature(0.7)
                .maxTokens(2000)
                .build();
        return llmRouter.chat(request);
    }

    /**
     * 调用 LLM（带历史消息）
     */
    protected LlmResponse callLlm(List<Message> messages) {
        LlmRequest request = LlmRequest.builder()
                .systemPrompt(systemPrompt)
                .messages(messages)
                .temperature(0.7)
                .maxTokens(2000)
                .build();
        return llmRouter.chat(request);
    }

    public String getName() {
        return name;
    }
}
