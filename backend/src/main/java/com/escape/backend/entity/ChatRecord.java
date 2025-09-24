package com.escape.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_records") // 映射表名
public class ChatRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("role")
    private String role; // 存 user / assistant

    @TableField("content_text")
    private String contentText;

    @TableField("content_audio_url")
    private String contentAudioUrl;

    @TableField("tokens_used")
    private Integer tokensUsed;

    @TableField("response_time_ms")
    private Integer responseTimeMs;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 便捷构造方法
    public ChatRecord(Long sessionId, String role, String contentText) {
        this.sessionId = sessionId;
        this.role = role;
        this.contentText = contentText;
    }

    public ChatRecord(Long sessionId, String role, String contentText, Integer tokensUsed, Integer responseTimeMs) {
        this.sessionId = sessionId;
        this.role = role;
        this.contentText = contentText;
        this.tokensUsed = tokensUsed;
        this.responseTimeMs = responseTimeMs;
    }
}
