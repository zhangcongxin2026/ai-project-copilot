package com.copilot.llm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * LLM 路由器测试
 */
class LlmRouterTest {

    private final LlmRouter router = new LlmRouter();

    @Test
    void testRouterInitialization() {
        assertNotNull(router);
        assertTrue(router.getAvailableProviders().isEmpty());
    }

    @Test
    void testRegisterClient() {
        LlmClient mockClient = new MockLlmClient();
        router.registerClient(LlmProvider.OLLAMA, mockClient);

        assertEquals(1, router.getAvailableProviders().size());
        assertTrue(router.getAvailableProviders().contains("OLLAMA"));
    }

    static class MockLlmClient implements LlmClient {
        @Override
        public com.copilot.model.LlmResponse chat(com.copilot.model.LlmRequest request) {
            return com.copilot.model.LlmResponse.builder()
                    .content("mock response")
                    .model("mock")
                    .cached(false)
                    .build();
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public String getProviderName() {
            return "Mock";
        }
    }
}
