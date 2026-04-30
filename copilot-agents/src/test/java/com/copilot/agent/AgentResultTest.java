package com.copilot.agent;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Agent 结果测试
 */
class AgentResultTest {

    @Test
    void testAgentResultSuccess() {
        AgentResult result = AgentResult.builder()
                .success(true)
                .output("测试输出")
                .build();

        assertTrue(result.isSuccess());
        assertEquals("测试输出", result.getOutput());
        assertNull(result.getErrorMessage());
    }

    @Test
    void testAgentResultFailure() {
        AgentResult result = AgentResult.builder()
                .success(false)
                .errorMessage("测试错误信息")
                .build();

        assertFalse(result.isSuccess());
        assertEquals("测试错误信息", result.getErrorMessage());
    }

    @Test
    void testAgentResultWithMetadata() {
        AgentResult result = AgentResult.builder()
                .success(true)
                .output("输出")
                .metadata(java.util.Map.of("key", "value"))
                .build();

        assertEquals("value", result.getMetadata().get("key"));
    }
}
