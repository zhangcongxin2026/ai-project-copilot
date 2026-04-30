package com.copilot.api;

import com.copilot.orchestrator.Orchestrator;
import com.copilot.orchestrator.Orchestrator.WorkflowResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST API 控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CopilotController {

    private final Orchestrator orchestrator;

    /**
     * 提交需求并启动工作流（异步）
     */
    @PostMapping("/workflow")
    public ResponseEntity<Map<String, String>> submitWorkflow(@RequestBody WorkflowRequest request) {
        String workflowId = "wf-" + System.currentTimeMillis();

        // 异步执行
        CompletableFuture<WorkflowResult> future = orchestrator.runAsync(request.getInput());

        // 存储 future 以便后续获取结果
        orchestrator.storeWorkflow(workflowId, future);

        return ResponseEntity.ok(Map.of(
                "workflowId", workflowId,
                "status", "started"
        ));
    }

    /**
     * 同步执行工作流（适合 Demo）
     */
    @PostMapping("/workflow/sync")
    public ResponseEntity<WorkflowResult> runSyncWorkflow(@RequestBody WorkflowRequest request) {
        WorkflowResult result = orchestrator.runFullWorkflow(request.getInput());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取工作流状态
     */
    @GetMapping("/workflow/{id}")
    public ResponseEntity<WorkflowResult> getWorkflowStatus(@PathVariable String id) {
        WorkflowResult result = orchestrator.getWorkflowStatus(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ai-project-copilot"
        ));
    }

    @Data
    public static class WorkflowRequest {
        private String input;
    }
}
