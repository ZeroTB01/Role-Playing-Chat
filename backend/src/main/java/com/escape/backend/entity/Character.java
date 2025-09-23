package com.escape.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@TableName("characters") // 指定表名
public class Character {

    @TableId
    private Long id;

    private String name;

    private String category;

    @TableField("avatar_url")
    private String avatarUrl;

    private String description;

    @TableField("personality_prompt")
    private String personalityPrompt;

    @TableField("voice_config")
    private String voiceConfig;

    private Integer popularity;

    @TableField("is_active")
    private Boolean isActive;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
