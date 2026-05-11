package com.copilot.agent;

import com.copilot.llm.LlmRouter;
import com.copilot.model.LlmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 代码审查 Agent
 * 对生成的代码进行质量审查，给出改进建议
 */
@Slf4j
@Component
public class ReviewAgent extends Agent {

    private static final String SYSTEM_PROMPT = """
            你是一个资深代码审查专家。你的任务是对代码进行全面审查并给出改进建议。

            请从以下维度审查代码：
            1. 代码质量：命名规范、结构清晰度、可读性
            2. 安全性：是否存在注入、XSS、敏感信息泄露等风险
            3. 性能：是否有明显的性能问题或可优化点
            4. 最佳实践：是否遵循语言/框架的最佳实践
            5. 可维护性：代码是否易于理解和修改

            请以 JSON 格式返回结果：
            {
                "score": 85,
                "summary": "总体评价",
                "issues": [
                    {
                        "severity": "HIGH|MEDIUM|LOW",
                        "category": "SECURITY|PERFORMANCE|QUALITY|BEST_PRACTICE",
                        "description": "问题描述",
                        "suggestion": "改进建议"
                    }
                ],
                "improvedCode": "改进后的代码（如有必要）"
            }
            """;

    public ReviewAgent(LlmRouter llmRouter) {
        super(llmRouter, "ReviewAgent", SYSTEM_PROMPT);
    }

    @Override
    public AgentResult execute(String input) {
        try {
            log.info("Reviewing code");

            LlmResponse response = callLlm(input);

            return AgentResult.builder()
                    .success(true)
                    .output(response.getContent())
                    .build();

        } catch (Exception e) {
            log.error("Code review failed", e);
            return AgentResult.builder()
                    .success(false)
                    .errorMessage("代码审查失败：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 审查指定代码
     */
    public AgentResult reviewCode(String code, String language, String context) {
        String prompt = buildPrompt(code, language, context);
        return execute(prompt);
    }

    private String buildPrompt(String code, String language, String context) {
        return """
                请审查以下%s代码：

                ```%s
                %s
                ```

                上下文信息：
                %s

                请给出详细的审查报告和改进建议。
                """.formatted(language, language, code, context != null ? context : "无");
    }
}
