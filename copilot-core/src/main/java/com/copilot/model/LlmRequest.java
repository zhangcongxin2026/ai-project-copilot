package com.copilot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LLM 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {
    private String model;
    private String systemPrompt;
    private String userPrompt;
    private List<Message> messages;
    private Double temperature;
    private Integer maxTokens;
}
