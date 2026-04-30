package com.copilot.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Agent 执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResult {
    private boolean success;
    private String output;
    private String errorMessage;
    private Map<String, Object> metadata;
}
