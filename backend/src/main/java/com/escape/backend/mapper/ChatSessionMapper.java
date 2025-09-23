package com.escape.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.backend.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}