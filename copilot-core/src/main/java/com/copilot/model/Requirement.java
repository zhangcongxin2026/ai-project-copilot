package com.copilot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 需求表示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Requirement {
    private String id;
    private String title;
    private String description;
    private RequirementPriority priority;
    private List<String> tags;
    private LocalDateTime createdAt;
}
