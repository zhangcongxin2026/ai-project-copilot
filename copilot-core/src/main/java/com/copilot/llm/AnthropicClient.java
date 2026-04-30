package com.copilot.llm;

import com.copilot.model.LlmRequest;
import com.copilot.model.LlmResponse;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.input.Prompt;
import lombok.extern.slf4j.Slf4j;

/**
 * Anthropic Claude 客户端实现
 */
@Slf4j
public class AnthropicClient implements LlmClient {

    private final AnthropicChatModel chatModel;
    private final String apiKey;

    public AnthropicClient(String apiKey, String modelName) {
        this.apiKey = apiKey;
        this.chatModel = AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName != null ? modelName : "claude-sonnet-4-20250514")
                .build();
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        try {
            Prompt prompt = Prompt.from(request.getUserPrompt());
            String response = chatModel.generate(prompt.text());
            return LlmResponse.builder()
                    .content(response)
                    .model("claude")
                    .cached(false)
                    .build();
        } catch (Exception e) {
            log.error("Anthropic chat error", e);
            throw new RuntimeException("Anthropic request failed", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public String getProviderName() {
        return "Anthropic";
    }
}
