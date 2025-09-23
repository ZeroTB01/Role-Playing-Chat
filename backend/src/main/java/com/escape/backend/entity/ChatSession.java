package com.escape.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "chat_sessions", autoResultMap = true)
public class ChatSession {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("character_id")
    private Long characterId;

    @TableField("session_token")
    private String sessionToken;

    @TableField("context_summary")
    private String contextSummary;

    @TableField(value = "started_at", fill = FieldFill.INSERT)
    private LocalDateTime startedAt;

    @TableField(value = "last_active_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastActiveAt;

    @TableField("status")
    private SessionStatus status = SessionStatus.ACTIVE;

    // 关联对象（非数据库字段，用于查询映射）
    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Character character;

    @TableField(exist = false)
    private List<ChatRecord> chatRecords;

    // 会话状态枚举
    public enum SessionStatus {
        ACTIVE("active"),
        ARCHIVED("archived");

        private final String value;

        SessionStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
