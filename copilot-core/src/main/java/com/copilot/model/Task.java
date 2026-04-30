package com.copilot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务表示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String id;
    private String title;
    private String description;
    private TaskStatus status;
    private String assignedAgent;
    private List<String> dependencies;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
