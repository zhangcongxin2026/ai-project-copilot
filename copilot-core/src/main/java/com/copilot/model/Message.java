package com.copilot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息表示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String role;  // system, user, assistant
    private String content;
}
