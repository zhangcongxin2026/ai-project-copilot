package com.copilot.agent;

import com.copilot.llm.LlmRouter;
import com.copilot.model.Task;
import com.copilot.model.TaskStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 任务生成 Agent
 * 将需求分解为可执行的任务列表
 */
@Slf4j
@Component
public class TaskAgent extends Agent {

    private static final String SYSTEM_PROMPT = """
            你是一个项目管理专家。你的任务是将需求分解为具体的可执行任务。

            请分析输入的需求并创建任务列表，每个任务应包含：
            1. 任务标题（简洁明了）
            2. 任务描述（具体要做什么）
            3. 依赖关系（如果有前置任务）

            请以 JSON 格式返回结果，格式如下：
            {
                "tasks": [
                    {
                        "title": "任务标题",
                        "description": "任务描述",
                        "dependencies": []
                    },
                    {
                        "title": "任务标题",
                        "description": "任务描述",
                        "dependencies": ["task-1"]
                    }
                ]
            }
            """;

    private final ObjectMapper objectMapper;

    public TaskAgent(LlmRouter llmRouter, ObjectMapper objectMapper) {
        super(llmRouter, "TaskAgent", SYSTEM_PROMPT);
        this.objectMapper = objectMapper;
    }

    @Override
    public AgentResult execute(String input) {
        try {
            log.info("Generating tasks for requirement");

            LlmResponse response = callLlm(input);
            String jsonStr = extractJson(response.getContent());

            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            List<Task> tasks = parseTasks(jsonNode);

            return AgentResult.builder()
                    .success(true)
                    .output(objectMapper.writeValueAsString(tasks))
                    .metadata(Map.of("taskCount", tasks.size()))
                    .build();

        } catch (Exception e) {
            log.error("Task generation failed", e);
            return AgentResult.builder()
                    .success(false)
                    .errorMessage("任务生成失败：" + e.getMessage())
                    .build();
        }
    }

    private List<Task> parseTasks(JsonNode jsonNode) {
        List<Task> tasks = new ArrayList<>();
        JsonNode tasksNode = jsonNode.path("tasks");

        int index = 0;
        for (JsonNode taskNode : tasksNode) {
            String title = taskNode.path("title").asText("未命名任务");
            String description = taskNode.path("description").asText("");
            List<String> dependencies = new ArrayList<>();
            taskNode.path("dependencies").forEach(dep -> dependencies.add(dep.asText()));

            Task task = Task.builder()
                    .id("task-" + (++index))
                    .title(title)
                    .description(description)
                    .status(TaskStatus.PENDING)
                    .assignedAgent("CodeAgent")
                    .dependencies(dependencies)
                    .createdAt(LocalDateTime.now())
                    .build();
            tasks.add(task);
        }

        return tasks;
    }

    private String extractJson(String content) {
        int start = content.indexOf("```json");
        if (start == -1) {
            start = content.indexOf("{");
        } else {
            start = content.indexOf("{", start);
        }

        int end = content.lastIndexOf("}");

        if (start != -1 && end != -1 && end > start) {
            return content.substring(start, end + 1);
        }

        return content;
    }
}
