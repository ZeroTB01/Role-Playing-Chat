package com.escape.backend.controller;

import com.escape.backend.dto.*;
import com.escape.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/text")
    public ApiResponse<TextChatResponse> chatText(@RequestBody TextChatRequest request){
        return ApiResponse.success(chatService.chatText(request));
    }

    @PostMapping("/voice")
    public ApiResponse<VoiceChatResponse> chatVoice(@RequestBody VoiceChatRequest request){
        return ApiResponse.success(chatService.chatVoice(request));
    }
}


