package com.copilot.orchestrator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Orchestrator 工作流结果测试
 */
class OrchestratorTest {

    @Test
    void testWorkflowResultCreation() {
        List<Orchestrator.StepResult> stepResults = List.of();

        Orchestrator.WorkflowResult result = new Orchestrator.WorkflowResult(
            "wf-123",
            "test input",
            Orchestrator.WorkflowStatus.COMPLETED,
            stepResults,
            null,
            100,
            0,
            List.of(),
            java.time.LocalDateTime.now()
        );

        assertEquals("wf-123", result.getId());
        assertEquals(Orchestrator.WorkflowStatus.COMPLETED, result.getStatus());
        assertEquals(100, result.getProgress());
        assertTrue(result.getLogs().isEmpty());
    }

    @Test
    void testWorkflowStatusEnum() {
        assertEquals(Orchestrator.WorkflowStatus.PENDING, Orchestrator.WorkflowStatus.PENDING);
        assertEquals(Orchestrator.WorkflowStatus.COMPLETED, Orchestrator.WorkflowStatus.COMPLETED);
        assertEquals(Orchestrator.WorkflowStatus.FAILED, Orchestrator.WorkflowStatus.FAILED);
    }
}
