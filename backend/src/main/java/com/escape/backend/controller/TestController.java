package com.escape.backend.controller;


import com.escape.backend.entity.Character;
import com.escape.backend.service.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private CharacterService characterService;
    // 测试查询所有角色
    @GetMapping({"/characters", "/characters/"})
    public List<Character> getAllCharacters() {
        return characterService.list();
    }

    @GetMapping("/characters/raw")
    public List<Map<String, Object>> getAllCharactersRaw() {
        return characterService.listMaps();
    }

    @GetMapping("/characters/ping")
    public String charactersPing(){
        return "characters-ok";
    }

    @GetMapping("/test")
    public String test(){
        return "rolePaly初始化成功了 : ) ";
    }
}
