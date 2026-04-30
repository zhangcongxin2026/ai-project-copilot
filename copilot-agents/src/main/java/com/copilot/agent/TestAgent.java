package com.copilot.agent;

import com.copilot.llm.LlmRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 测试生成 Agent
 * 根据代码生成测试用例
 */
@Slf4j
@Component
public class TestAgent extends Agent {

    private static final String SYSTEM_PROMPT = """
            你是一个测试专家。你的任务是根据代码生成全面的测试用例。

            请生成：
            1. 单元测试
            2. 边界条件测试
            3. 异常场景测试

            请以代码块格式返回结果：
            ```language
            // 测试代码
            ```
            """;

    public TestAgent(LlmRouter llmRouter) {
        super(llmRouter, "TestAgent", SYSTEM_PROMPT);
    }

    @Override
    public AgentResult execute(String input) {
        try {
            log.info("Generating tests");

            LlmResponse response = callLlm(input);

            return AgentResult.builder()
                    .success(true)
                    .output(response.getContent())
                    .build();

        } catch (Exception e) {
            log.error("Test generation failed", e);
            return AgentResult.builder()
                    .success(false)
                    .errorMessage("测试生成失败：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 为特定代码生成测试
     */
    public AgentResult generateTestForCode(String code, String language) {
        String prompt = buildPrompt(code, language);
        return execute(prompt);
    }

    private String buildPrompt(String code, String language) {
        return """
                请为以下%s代码生成测试用例：

                ```%s
                %s
                ```

                请生成完整的、可运行的测试代码。
                """.formatted(language, language, code);
    }
}
