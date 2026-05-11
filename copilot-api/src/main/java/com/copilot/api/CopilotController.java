package com.copilot.api;

import com.copilot.orchestrator.Orchestrator;
import com.copilot.orchestrator.Orchestrator.WorkflowResult;
import com.copilot.rag.RagEngine;
import com.copilot.rag.RagEngine.SearchResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API 控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CopilotController {

    private final Orchestrator orchestrator;
    private final RagEngine ragEngine;

    /**
     * 提交需求并启动工作流（异步）
     */
    @PostMapping("/workflow")
    public ResponseEntity<Map<String, String>> submitWorkflow(@RequestBody WorkflowRequest request) {
        String workflowId = "wf-" + System.currentTimeMillis();

        // 异步执行（传入统一 ID）
        orchestrator.runAsync(workflowId, request.getInput());

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

    /**
     * 获取所有工作流历史
     */
    @GetMapping("/workflows")
    public ResponseEntity<List<WorkflowResult>> listWorkflows() {
        return ResponseEntity.ok(orchestrator.getAllWorkflows());
    }

    // ==================== RAG 文档管理 ====================

    /**
     * 添加文档到知识库
     */
    @PostMapping("/rag/documents")
    public ResponseEntity<Map<String, String>> addDocument(@RequestBody RagDocumentRequest request) {
        String id = request.getId() != null ? request.getId() : UUID.randomUUID().toString();
        ragEngine.addDocument(id, request.getContent(), request.getMetadata());
        return ResponseEntity.ok(Map.of("id", id, "status", "added"));
    }

    /**
     * 搜索知识库
     */
    @GetMapping("/rag/search")
    public ResponseEntity<List<SearchResult>> searchDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int maxResults) {
        List<SearchResult> results = ragEngine.search(query, maxResults);
        return ResponseEntity.ok(results);
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/rag/documents/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable String id) {
        ragEngine.deleteDocument(id);
        return ResponseEntity.ok(Map.of("id", id, "status", "deleted"));
    }

    /**
     * 清空知识库
     */
    @DeleteMapping("/rag/documents")
    public ResponseEntity<Map<String, String>> clearDocuments() {
        ragEngine.clear();
        return ResponseEntity.ok(Map.of("status", "cleared"));
    }

    @Data
    public static class WorkflowRequest {
        private String input;
    }

    @Data
    public static class RagDocumentRequest {
        private String id;
        private String content;
        private Map<String, String> metadata;
    }
}
