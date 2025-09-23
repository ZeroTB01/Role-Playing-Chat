package com.escape.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.escape.backend.entity.Character;
import com.escape.backend.mapper.CharacterMapper;
import com.escape.backend.service.CharacterService;
import org.springframework.stereotype.Service;

@Service
public class CharacterServiceImpl extends ServiceImpl<CharacterMapper, Character> implements CharacterService {
}


