package com.copilot.config;

import com.copilot.agent.*;
import com.copilot.llm.AnthropicClient;
import com.copilot.llm.LlmClient;
import com.copilot.llm.LlmProvider;
import com.copilot.llm.LlmRouter;
import com.copilot.llm.OllamaClient;
import com.copilot.llm.OpenAiClient;
import com.copilot.rag.EmbeddingService;
import com.copilot.rag.LocalEmbeddingService;
import com.copilot.rag.RagEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring 配置类
 */
@Configuration
public class AppConfig {

    @Value("${llm.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${llm.anthropic.model-name:}")
    private String anthropicModelName;

    @Value("${llm.openai.api-key:}")
    private String openaiApiKey;

    @Value("${llm.ollama.base-url:}")
    private String ollamaBaseUrl;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public EmbeddingService embeddingService() {
        return new LocalEmbeddingService();
    }

    @Bean
    public RagEngine ragEngine(EmbeddingService embeddingService) {
        return new RagEngine(embeddingService);
    }

    @Bean
    public LlmRouter llmRouter() {
        LlmRouter router = new LlmRouter();

        // 注册可用的 LLM 客户端
        if (!anthropicApiKey.isEmpty()) {
            router.registerClient(LlmProvider.ANTHROPIC, new AnthropicClient(anthropicApiKey, anthropicModelName));
        }
        if (!openaiApiKey.isEmpty()) {
            router.registerClient(LlmProvider.OPENAI, new OpenAiClient(openaiApiKey, "gpt-4o"));
        }
        router.registerClient(LlmProvider.OLLAMA, new OllamaClient(ollamaBaseUrl, "llama3"));

        return router;
    }

    @Bean
    public RequirementAgent requirementAgent(LlmRouter llmRouter, ObjectMapper objectMapper) {
        return new RequirementAgent(llmRouter, objectMapper);
    }

    @Bean
    public TaskAgent taskAgent(LlmRouter llmRouter, ObjectMapper objectMapper) {
        return new TaskAgent(llmRouter, objectMapper);
    }

    @Bean
    public CodeAgent codeAgent(LlmRouter llmRouter) {
        return new CodeAgent(llmRouter);
    }

    @Bean
    public TestAgent testAgent(LlmRouter llmRouter) {
        return new TestAgent(llmRouter);
    }
}
