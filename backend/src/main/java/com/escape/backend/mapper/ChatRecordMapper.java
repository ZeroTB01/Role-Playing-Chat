package com.escape.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.backend.entity.ChatRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatRecordMapper extends BaseMapper<ChatRecord> {
}