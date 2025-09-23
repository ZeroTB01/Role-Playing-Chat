package com.escape.backend.service.impl;

import com.escape.backend.dto.TextChatRequest;
import com.escape.backend.dto.TextChatResponse;
import com.escape.backend.dto.VoiceChatRequest;
import com.escape.backend.dto.VoiceChatResponse;
import com.escape.backend.service.ChatService;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {
    @Override
    public TextChatResponse chatText(TextChatRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public VoiceChatResponse chatVoice(VoiceChatRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}


