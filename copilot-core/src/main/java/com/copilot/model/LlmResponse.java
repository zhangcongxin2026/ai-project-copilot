package com.copilot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {
    private String content;
    private String model;
    private Integer tokensUsed;
    private Double latencyMs;
    private boolean cached;
}
