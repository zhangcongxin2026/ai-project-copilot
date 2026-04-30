package com.copilot.agent;

import com.copilot.llm.LlmRouter;
import com.copilot.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成 Agent
 * 根据任务描述生成代码
 */
@Slf4j
@Component
public class CodeAgent extends Agent {

    private static final String SYSTEM_PROMPT = """
            你是一个高级软件工程师。你的任务是根据任务描述生成高质量的代码。

            请生成：
            1. 清晰、可读性好的代码
            2. 必要的注释（只注释为什么，不注释是什么）
            3. 遵循最佳实践

            请以代码块格式返回结果：
            ```language
            // 你的代码
            ```
            """;

    public CodeAgent(LlmRouter llmRouter) {
        super(llmRouter, "CodeAgent", SYSTEM_PROMPT);
    }

    @Override
    public AgentResult execute(String input) {
        try {
            log.info("Generating code for task");

            LlmResponse response = callLlm(input);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("codeLength", response.getContent().length());

            return AgentResult.builder()
                    .success(true)
                    .output(response.getContent())
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            log.error("Code generation failed", e);
            return AgentResult.builder()
                    .success(false)
                    .errorMessage("代码生成失败：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 为特定任务生成代码
     */
    public AgentResult executeForTask(Task task, String context) {
        String prompt = buildPrompt(task, context);
        return execute(prompt);
    }

    private String buildPrompt(Task task, String context) {
        return """
                请为以下任务生成代码：

                任务：%s
                描述：%s

                上下文信息：
                %s

                请生成完整的、可运行的代码。
                """.formatted(task.getTitle(), task.getDescription(), context != null ? context : "无");
    }
}
