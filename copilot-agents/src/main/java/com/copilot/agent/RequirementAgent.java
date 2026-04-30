package com.copilot.agent;

import com.copilot.llm.LlmRouter;
import com.copilot.model.Requirement;
import com.copilot.model.RequirementPriority;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 需求解析 Agent
 * 将用户输入的需求转换为结构化的需求对象
 */
@Slf4j
@Component
public class RequirementAgent extends Agent {

    private static final String SYSTEM_PROMPT = """
            你是一个需求分析专家。你的任务是将用户的需求描述转换为结构化的格式。

            请分析输入并提取以下信息：
            1. 需求的标题（简洁概括）
            2. 详细描述
            3. 优先级（LOW/MEDIUM/HIGH/CRITICAL）
            4. 相关标签（3-5 个关键词）

            请以 JSON 格式返回结果，格式如下：
            {
                "title": "需求标题",
                "description": "详细描述",
                "priority": "优先级",
                "tags": ["标签 1", "标签 2", "标签 3"]
            }
            """;

    private final ObjectMapper objectMapper;

    public RequirementAgent(LlmRouter llmRouter, ObjectMapper objectMapper) {
        super(llmRouter, "RequirementAgent", SYSTEM_PROMPT);
        this.objectMapper = objectMapper;
    }

    @Override
    public AgentResult execute(String input) {
        try {
            log.info("Analyzing requirement: {}", input.substring(0, Math.min(100, input.length())));

            LlmResponse response = callLlm(input);
            String jsonStr = extractJson(response.getContent());

            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            Requirement requirement = parseRequirement(jsonNode);

            return AgentResult.builder()
                    .success(true)
                    .output(objectMapper.writeValueAsString(requirement))
                    .metadata(Map.of("requirementId", requirement.getId()))
                    .build();

        } catch (Exception e) {
            log.error("Requirement analysis failed", e);
            return AgentResult.builder()
                    .success(false)
                    .errorMessage("需求解析失败：" + e.getMessage())
                    .build();
        }
    }

    private Requirement parseRequirement(JsonNode jsonNode) {
        String title = jsonNode.path("title").asText("未命名需求");
        String description = jsonNode.path("description").asText("");
        String priorityStr = jsonNode.path("priority").asText("MEDIUM");

        RequirementPriority priority;
        try {
            priority = RequirementPriority.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            priority = RequirementPriority.MEDIUM;
        }

        List<String> tags = new ArrayList<>();
        jsonNode.path("tags").forEach(node -> tags.add(node.asText()));

        return Requirement.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .priority(priority)
                .tags(tags)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private String extractJson(String content) {
        // 提取 JSON 代码块
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
