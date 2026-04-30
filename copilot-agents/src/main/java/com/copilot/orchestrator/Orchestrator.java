package com.copilot.orchestrator;

import com.copilot.agent.AgentResult;
import com.copilot.agent.CodeAgent;
import com.copilot.agent.RequirementAgent;
import com.copilot.agent.TaskAgent;
import com.copilot.model.Task;
import com.copilot.model.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 任务编排器
 * 协调多个 Agent 完成从需求到任务的自动化流程
 */
@Slf4j
@Component
public class Orchestrator {

    private final RequirementAgent requirementAgent;
    private final TaskAgent taskAgent;
    private final CodeAgent codeAgent;
    private final ConcurrentMap<String, WorkflowInstance> workflows = new ConcurrentHashMap<>();

    public Orchestrator(RequirementAgent requirementAgent, TaskAgent taskAgent, CodeAgent codeAgent) {
        this.requirementAgent = requirementAgent;
        this.taskAgent = taskAgent;
        this.codeAgent = codeAgent;
    }

    /**
     * 启动完整的工作流：需求 -> 任务分解 -> 代码生成
     */
    public WorkflowResult runFullWorkflow(String requirementInput) {
        String workflowId = "wf-" + System.currentTimeMillis();
        WorkflowInstance workflow = new WorkflowInstance(workflowId, requirementInput);
        workflows.put(workflowId, workflow);

        try {
            // 步骤 1: 需求解析
            workflow.updateStatus(WorkflowStatus.PARSING_REQUIREMENT);
            AgentResult requirementResult = requirementAgent.execute(requirementInput);
            if (!requirementResult.isSuccess()) {
                throw new RuntimeException("需求解析失败：" + requirementResult.getErrorMessage());
            }
            workflow.addStepResult("requirement", requirementResult);

            // 步骤 2: 任务分解
            workflow.updateStatus(WorkflowStatus.GENERATING_TASKS);
            AgentResult taskResult = taskAgent.execute(requirementResult.getOutput());
            if (!taskResult.isSuccess()) {
                throw new RuntimeException("任务生成失败：" + taskResult.getErrorMessage());
            }
            workflow.addStepResult("tasks", taskResult);

            // 步骤 3: 代码生成（简单示例，实际应该并行处理多个任务）
            workflow.updateStatus(WorkflowStatus.GENERATING_CODE);
            List<Task> tasks = parseTasks(taskResult.getOutput());
            for (Task task : tasks) {
                AgentResult codeResult = codeAgent.execute("为以下任务生成代码：" + task.getTitle() + " - " + task.getDescription());
                workflow.addStepResult("code-" + task.getId(), codeResult);
            }

            workflow.updateStatus(WorkflowStatus.COMPLETED);
            log.info("Workflow completed: {}", workflowId);

        } catch (Exception e) {
            workflow.updateStatus(WorkflowStatus.FAILED);
            workflow.setErrorMessage(e.getMessage());
            log.error("Workflow failed: {}", workflowId, e);
        }

        return workflow.toResult();
    }

    /**
     * 异步执行工作流
     */
    public CompletableFuture<WorkflowResult> runAsync(String requirementInput) {
        return CompletableFuture.supplyAsync(() -> runFullWorkflow(requirementInput));
    }

    /**
     * 获取工作流状态
     */
    public WorkflowResult getWorkflowStatus(String workflowId) {
        WorkflowInstance workflow = workflows.get(workflowId);
        if (workflow == null) {
            return null;
        }
        return workflow.toResult();
    }

    private List<Task> parseTasks(String json) {
        // 简化实现，实际应该使用 JSON 解析
        return new ArrayList<>();
    }

    /**
     * 工作流状态
     */
    public enum WorkflowStatus {
        PENDING,
        PARSING_REQUIREMENT,
        GENERATING_TASKS,
        GENERATING_CODE,
        COMPLETED,
        FAILED
    }

    /**
     * 工作流实例
     */
    static class WorkflowInstance {
        private final String id;
        private final String input;
        private WorkflowStatus status;
        private final List<StepResult> stepResults = new ArrayList<>();
        private String errorMessage;

        public WorkflowInstance(String id, String input) {
            this.id = id;
            this.input = input;
            this.status = WorkflowStatus.PENDING;
        }

        public void updateStatus(WorkflowStatus status) {
            this.status = status;
        }

        public void addStepResult(String stepName, AgentResult result) {
            stepResults.add(new StepResult(stepName, result));
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public WorkflowResult toResult() {
            return new WorkflowResult(id, input, status, stepResults, errorMessage);
        }
    }

    /**
     * 步骤结果
     */
    record StepResult(String stepName, AgentResult result) {}

    /**
     * 工作流结果
     */
    public record WorkflowResult(
            String id,
            String input,
            WorkflowStatus status,
            List<StepResult> stepResults,
            String errorMessage
    ) {}
}
