package com.escape.backend.controller;

import com.escape.backend.dto.ApiResponse;
import com.escape.backend.entity.Character;
import com.escape.backend.service.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/character")
public class CharacterController {

    @Autowired
    private CharacterService characterService;

    @GetMapping("/list")
    public ApiResponse<List<Character>> listCharacters() {
        return ApiResponse.success(characterService.list());
    }
}


