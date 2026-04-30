package com.copilot.llm;

import com.copilot.model.LlmRequest;
import com.copilot.model.LlmResponse;

/**
 * LLM 客户端接口
 */
public interface LlmClient {
    /**
     * 发送请求并获取响应
     */
    LlmResponse chat(LlmRequest request);

    /**
     * 检查客户端是否可用
     */
    boolean isAvailable();

    /**
     * 获取提供者名称
     */
    String getProviderName();
}
