package com.copilot.llm;

import com.copilot.model.LlmRequest;
import com.copilot.model.LlmResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.input.Prompt;
import lombok.extern.slf4j.Slf4j;

/**
 * Ollama 本地模型客户端实现
 */
@Slf4j
public class OllamaClient implements LlmClient {

    private final OllamaChatModel chatModel;
    private final String baseUrl;

    public OllamaClient(String baseUrl, String modelName) {
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:11434";
        this.chatModel = OllamaChatModel.builder()
                .baseUrl(this.baseUrl)
                .modelName(modelName != null ? modelName : "llama3")
                .build();
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        try {
            Prompt prompt = Prompt.from(request.getUserPrompt());
            String response = chatModel.generate(prompt.text());
            return LlmResponse.builder()
                    .content(response)
                    .model("ollama")
                    .cached(false)
                    .build();
        } catch (Exception e) {
            log.error("Ollama chat error", e);
            throw new RuntimeException("Ollama request failed", e);
        }
    }

    @Override
    public boolean isAvailable() {
        // 简单检查，实际应该 ping 一下服务
        return true;
    }

    @Override
    public String getProviderName() {
        return "Ollama";
    }
}
