package com.copilot.orchestrator;

import com.copilot.agent.AgentResult;
import com.copilot.agent.CodeAgent;
import com.copilot.agent.RequirementAgent;
import com.copilot.agent.ReviewAgent;
import com.copilot.agent.TaskAgent;
import com.copilot.model.Task;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;

/**
 * 任务编排器 - 增强版
 * 支持并行执行、任务依赖解析、实时进度跟踪
 */
@Slf4j
@Component
public class Orchestrator {

    private final RequirementAgent requirementAgent;
    private final TaskAgent taskAgent;
    private final CodeAgent codeAgent;
    private final ReviewAgent reviewAgent;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentMap<String, WorkflowInstance> workflows = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public Orchestrator(RequirementAgent requirementAgent, TaskAgent taskAgent, CodeAgent codeAgent,
                        ReviewAgent reviewAgent, SimpMessagingTemplate messagingTemplate) {
        this.requirementAgent = requirementAgent;
        this.taskAgent = taskAgent;
        this.codeAgent = codeAgent;
        this.reviewAgent = reviewAgent;
        this.messagingTemplate = messagingTemplate;
        // 创建固定大小的线程池用于并行执行任务
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2,
                new ThreadFactoryBuilder("copilot-worker")
        );
    }

    /**
     * 异步执行工作流
     */
    public CompletableFuture<WorkflowResult> runAsync(String requirementInput) {
        String workflowId = "wf-" + System.currentTimeMillis();
        return runAsync(workflowId, requirementInput);
    }

    /**
     * 异步执行工作流（指定 ID）
     */
    public CompletableFuture<WorkflowResult> runAsync(String workflowId, String requirementInput) {
        WorkflowInstance workflow = new WorkflowInstance(workflowId, requirementInput);
        workflows.put(workflowId, workflow);

        CompletableFuture<WorkflowResult> future = CompletableFuture.supplyAsync(
            () -> runFullWorkflowInternal(requirementInput, workflow),
            executorService
        );

        // Update status when done
        future.whenComplete((result, error) -> {
            if (result != null) {
                workflow.updateStatus(result.getStatus());
                workflow.setProgress(result.getProgress());
                broadcastUpdate(workflowId);
            }
        });

        return future;
    }

    /**
     * 存储工作流（用于外部创建 workflowId 的场景）
     */
    public void storeWorkflow(String workflowId, CompletableFuture<WorkflowResult> future) {
        WorkflowInstance workflow = new WorkflowInstance(workflowId, "");
        workflow.updateStatus(WorkflowStatus.PENDING);
        workflows.put(workflowId, workflow);

        // 当 future 完成时，更新状态
        future.whenComplete((result, error) -> {
            if (result != null) {
                WorkflowInstance wf = workflows.get(workflowId);
                if (wf != null) {
                    wf.updateStatus(result.getStatus());
                    wf.setProgress(result.getProgress());
                }
            }
        });
    }

    /**
     * 内部执行方法，返回结果
     */
    private WorkflowResult runFullWorkflowInternal(String requirementInput, WorkflowInstance workflow) {
        try {
            // 步骤 1: 需求解析
            workflow.updateStatus(WorkflowStatus.PARSING_REQUIREMENT);
            workflow.setProgress(10);
            broadcastUpdate(workflow.getId());
            AgentResult requirementResult = requirementAgent.execute(requirementInput);
            if (!requirementResult.isSuccess()) {
                throw new RuntimeException("需求解析失败：" + requirementResult.getErrorMessage());
            }
            workflow.addStepResult("requirement", requirementResult);
            workflow.setProgress(25);
            broadcastUpdate(workflow.getId());

            // 步骤 2: 任务分解
            workflow.updateStatus(WorkflowStatus.GENERATING_TASKS);
            workflow.setProgress(35);
            broadcastUpdate(workflow.getId());
            AgentResult taskResult = taskAgent.execute(requirementResult.getOutput());
            if (!taskResult.isSuccess()) {
                throw new RuntimeException("任务生成失败：" + taskResult.getErrorMessage());
            }
            workflow.addStepResult("tasks", taskResult);

            // 解析任务列表
            List<Task> tasks = parseTasks(taskResult.getOutput());
            workflow.setTaskCount(tasks.size());
            workflow.setProgress(50);
            broadcastUpdate(workflow.getId());

            // 步骤 3: 代码生成（并行执行，处理依赖）
            workflow.updateStatus(WorkflowStatus.GENERATING_CODE);
            broadcastUpdate(workflow.getId());
            executeTasksWithDependencies(workflow, tasks);

            // 步骤 4: 代码审查
            workflow.updateStatus(WorkflowStatus.REVIEWING_CODE);
            workflow.setProgress(85);
            broadcastUpdate(workflow.getId());
            StringBuilder allCode = new StringBuilder();
            for (StepResult sr : workflow.getStepResults()) {
                if (sr.getStepName().startsWith("code-") && sr.getResult().isSuccess()) {
                    allCode.append(sr.getResult().getOutput()).append("\n\n");
                }
            }
            if (allCode.length() > 0) {
                AgentResult reviewResult = reviewAgent.execute(
                    "请审查以下代码：\n" + allCode
                );
                workflow.addStepResult("review", reviewResult);
            }
            workflow.setProgress(95);

            workflow.updateStatus(WorkflowStatus.COMPLETED);
            workflow.setProgress(100);
            broadcastUpdate(workflow.getId());
            log.info("Workflow completed: {}", workflow.getId());

        } catch (Exception e) {
            workflow.updateStatus(WorkflowStatus.FAILED);
            workflow.setErrorMessage(e.getMessage());
            broadcastUpdate(workflow.getId());
            log.error("Workflow failed: {}", workflow.getId(), e);
        }

        return workflow.toResult();
    }

    /**
     * 启动完整的工作流：需求 -> 任务分解 -> 代码生成
     * @deprecated 使用 runAsync 替代
     */
    @Deprecated
    public WorkflowResult runFullWorkflow(String requirementInput) {
        String workflowId = "wf-" + System.currentTimeMillis();
        WorkflowInstance workflow = new WorkflowInstance(workflowId, requirementInput);
        workflows.put(workflowId, workflow);
        return runFullWorkflowInternal(requirementInput, workflow);
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

    /**
     * 获取所有工作流历史
     */
    public List<WorkflowResult> getAllWorkflows() {
        return workflows.values().stream()
                .map(WorkflowInstance::toResult)
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 广播工作流状态更新
     */
    private void broadcastUpdate(String workflowId) {
        WorkflowInstance workflow = workflows.get(workflowId);
        if (workflow != null && messagingTemplate != null) {
            try {
                messagingTemplate.convertAndSend("/topic/workflow/" + workflowId, workflow.toResult());
            } catch (Exception e) {
                log.warn("Failed to broadcast workflow update: {}", workflowId, e);
            }
        }
    }

    /**
     * 执行任务并处理依赖关系
     */
    private void executeTasksWithDependencies(WorkflowInstance workflow, List<Task> tasks) throws InterruptedException {
        if (tasks.isEmpty()) {
            return;
        }

        // 构建任务依赖图
        Map<String, Task> taskMap = new HashMap<>();
        Map<String, List<String>> dependents = new HashMap<>(); // 谁依赖我
        Map<String, Integer> dependencyCount = new HashMap<>();  // 我依赖多少个任务

        for (Task task : tasks) {
            taskMap.put(task.getId(), task);
            dependents.put(task.getId(), new ArrayList<>());
            dependencyCount.put(task.getId(), task.getDependencies().size());
        }

        // 建立反向依赖关系
        for (Task task : tasks) {
            for (String depId : task.getDependencies()) {
                dependents.computeIfAbsent(depId, k -> new ArrayList<>()).add(task.getId());
            }
        }

        // 就绪队列：没有依赖的任务
        Queue<String> readyQueue = new ConcurrentLinkedQueue<>();
        for (Task task : tasks) {
            if (dependencyCount.get(task.getId()) == 0) {
                readyQueue.offer(task.getId());
            }
        }

        // 已完成任务
        Set<String> completedTasks = ConcurrentHashMap.newKeySet();
        // 任务结果
        Map<String, AgentResult> taskResults = new ConcurrentHashMap<>();

        // 并行执行就绪任务
        AtomicInteger processedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(Math.max(1, readyQueue.size()));

        while (!readyQueue.isEmpty() || processedCount.get() < tasks.size()) {
            List<String> batch = new ArrayList<>();
            while (!readyQueue.isEmpty()) {
                batch.add(readyQueue.poll());
            }

            if (batch.isEmpty() && processedCount.get() < tasks.size()) {
                // 存在循环依赖
                log.warn("Circular dependency detected in workflow");
                break;
            }

            CountDownLatch batchLatch = new CountDownLatch(batch.size());
            for (String taskId : batch) {
                executorService.submit(() -> {
                    try {
                        Task task = taskMap.get(taskId);
                        String stepName = "code-" + task.getId();

                        workflow.addLog("开始执行任务：" + task.getTitle());

                        AgentResult result = codeAgent.execute(
                            "为以下任务生成代码：" + task.getTitle() + " - " + task.getDescription()
                        );

                        taskResults.put(taskId, result);
                        workflow.addStepResult(stepName, result);
                        completedTasks.add(taskId);

                        int count = processedCount.incrementAndGet();
                        int progress = 50 + (count * 35 / tasks.size());
                        workflow.setProgress(progress);

                        log.info("Task completed: {} ({}/{})", taskId, count, tasks.size());

                        // 更新依赖此任务的其他任务
                        synchronized (dependents) {
                            for (String dependentId : dependents.get(taskId)) {
                                int count = dependencyCount.decrementAndGet(dependentId);
                                if (count == 0 && !completedTasks.contains(dependentId)) {
                                    readyQueue.offer(dependentId);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Task execution failed: {}", taskId, e);
                    } finally {
                        batchLatch.countDown();
                    }
                });
            }

            batchLatch.await();
        }
    }

    /**
     * 解析任务列表
     */
    private List<Task> parseTasks(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(json);
            List<Task> tasks = new ArrayList<>();

            if (node.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode taskNode : node) {
                    tasks.add(parseTask(taskNode));
                }
            } else if (node.has("tasks")) {
                for (com.fasterxml.jackson.databind.JsonNode taskNode : node.get("tasks")) {
                    tasks.add(parseTask(taskNode));
                }
            }

            return tasks;
        } catch (Exception e) {
            log.error("Failed to parse tasks", e);
            return new ArrayList<>();
        }
    }

    private Task parseTask(com.fasterxml.jackson.databind.JsonNode node) {
        String id = node.path("id").asText("task-" + UUID.randomUUID().toString().substring(0, 8));
        String title = node.path("title").asText("未命名任务");
        String description = node.path("description").asText("");
        List<String> dependencies = new ArrayList<>();
        node.path("dependencies").forEach(dep -> dependencies.add(dep.asText()));

        return Task.builder()
                .id(id)
                .title(title)
                .description(description)
                .dependencies(dependencies)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 关闭执行器
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    /**
     * 线程工厂构建器
     */
    static class ThreadFactoryBuilder implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger counter = new AtomicInteger(1);

        public ThreadFactoryBuilder(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(namePrefix + "-thread-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }

    /**
     * 工作流实例
     */
    @Data
    static class WorkflowInstance {
        private final String id;
        private final String input;
        private WorkflowStatus status;
        private final List<StepResult> stepResults = Collections.synchronizedList(new ArrayList<>());
        private final List<String> logs = Collections.synchronizedList(new ArrayList<>());
        private String errorMessage;
        private int progress;
        private int taskCount;
        private LocalDateTime startedAt;

        public WorkflowInstance(String id, String input) {
            this.id = id;
            this.input = input;
            this.status = WorkflowStatus.PENDING;
            this.startedAt = LocalDateTime.now();
        }

        public void updateStatus(WorkflowStatus status) {
            this.status = status;
            addLog("状态更新：" + status);
        }

        public void addStepResult(String stepName, AgentResult result) {
            stepResults.add(new StepResult(stepName, result));
        }

        public void addLog(String message) {
            logs.add(LocalDateTime.now() + " - " + message);
        }

        public void setProgress(int progress) {
            this.progress = Math.min(100, Math.max(0, progress));
        }

        public WorkflowResult toResult() {
            return new WorkflowResult(
                    id, input, status, stepResults, errorMessage,
                    progress, taskCount, logs, startedAt
            );
        }
    }

    /**
     * 工作流状态
     */
    public enum WorkflowStatus {
        PENDING,
        PARSING_REQUIREMENT,
        GENERATING_TASKS,
        GENERATING_CODE,
        REVIEWING_CODE,
        COMPLETED,
        FAILED
    }

    /**
     * 步骤结果
     */
    @Data
    @AllArgsConstructor
    static class StepResult {
        private String stepName;
        private AgentResult result;
    }

    /**
     * 工作流结果
     */
    @Data
    @AllArgsConstructor
    public static class WorkflowResult {
        private String id;
        private String input;
        private WorkflowStatus status;
        private List<StepResult> stepResults;
        private String errorMessage;
        private int progress;
        private int taskCount;
        private List<String> logs;
        private LocalDateTime startedAt;
    }
}
