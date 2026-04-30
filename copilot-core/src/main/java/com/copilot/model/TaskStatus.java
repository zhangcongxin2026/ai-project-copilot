package com.copilot.model;

/**
 * 任务状态
 */
public enum TaskStatus {
    PENDING,      // 等待执行
    IN_PROGRESS,  // 执行中
    COMPLETED,    // 已完成
    FAILED,       // 失败
    CANCELLED     // 已取消
}
