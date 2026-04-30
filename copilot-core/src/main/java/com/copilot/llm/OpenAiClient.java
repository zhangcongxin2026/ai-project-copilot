package com.copilot.llm;

import com.copilot.model.LlmRequest;
import com.copilot.model.LlmResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.input.Prompt;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI GPT 客户端实现
 */
@Slf4j
public class OpenAiClient implements LlmClient {

    private final OpenAiChatModel chatModel;
    private final String apiKey;

    public OpenAiClient(String apiKey, String modelName) {
        this.apiKey = apiKey;
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName != null ? modelName : "gpt-4o")
                .build();
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        try {
            Prompt prompt = Prompt.from(request.getUserPrompt());
            String response = chatModel.generate(prompt.text());
            return LlmResponse.builder()
                    .content(response)
                    .model("gpt")
                    .cached(false)
                    .build();
        } catch (Exception e) {
            log.error("OpenAI chat error", e);
            throw new RuntimeException("OpenAI request failed", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
    }
}
