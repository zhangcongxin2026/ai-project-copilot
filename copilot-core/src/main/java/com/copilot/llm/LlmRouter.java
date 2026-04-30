package com.copilot.llm;

import com.copilot.model.LlmProvider;
import com.copilot.model.LlmRequest;
import com.copilot.model.LlmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LLM 路由器 - 支持多模型和自动故障转移
 */
@Slf4j
@Component
public class LlmRouter {

    private final Map<LlmProvider, LlmClient> clients = new ConcurrentHashMap<>();
    private LlmProvider currentProvider;

    public LlmRouter() {
        this.currentProvider = LlmProvider.AUTO;
    }

    /**
     * 注册 LLM 客户端
     */
    public void registerClient(LlmProvider provider, LlmClient client) {
        clients.put(provider, client);
        log.info("Registered LLM client: {}", provider);
    }

    /**
     * 设置当前使用的提供者
     */
    public void setCurrentProvider(LlmProvider provider) {
        this.currentProvider = provider;
    }

    /**
     * 发送请求，支持自动故障转移
     */
    public LlmResponse chat(LlmRequest request) {
        List<LlmProvider> triedProviders = new ArrayList<>();

        // 确定尝试的提供者列表
        List<LlmProvider> providersToTry = getProvidersToTry();

        for (LlmProvider provider : providersToTry) {
            if (triedProviders.contains(provider)) {
                continue;
            }

            LlmClient client = clients.get(provider);
            if (client == null || !client.isAvailable()) {
                log.warn("LLM client {} not available", provider);
                continue;
            }

            try {
                log.info("Using LLM provider: {}", provider);
                LlmResponse response = client.chat(request);
                return response;
            } catch (Exception e) {
                log.error("LLM provider {} failed, trying next", provider, e);
                triedProviders.add(provider);
            }
        }

        throw new RuntimeException("All LLM providers failed. Tried: " + triedProviders);
    }

    /**
     * 获取要尝试的提供者列表
     */
    private List<LlmProvider> getProvidersToTry() {
        if (currentProvider == LlmProvider.AUTO) {
            // 自动模式下，按优先级尝试
            List<LlmProvider> priority = new ArrayList<>();
            priority.add(LlmProvider.ANTHROPIC);
            priority.add(LlmProvider.OPENAI);
            priority.add(LlmProvider.OLLAMA);
            return priority;
        } else {
            return List.of(currentProvider);
        }
    }

    /**
     * 获取所有已注册的提供者
     */
    public List<String> getAvailableProviders() {
        List<String> available = new ArrayList<>();
        for (Map.Entry<LlmProvider, LlmClient> entry : clients.entrySet()) {
            if (entry.getValue().isAvailable()) {
                available.add(entry.getKey().name());
            }
        }
        return available;
    }
}
