package com.escape.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.backend.entity.Character;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CharacterMapper extends BaseMapper<Character> {
}
