package com.escape.backend.service;

import com.escape.backend.dto.TextChatRequest;
import com.escape.backend.dto.TextChatResponse;
import com.escape.backend.dto.VoiceChatRequest;
import com.escape.backend.dto.VoiceChatResponse;

public interface ChatService {
    TextChatResponse chatText(TextChatRequest request);
    VoiceChatResponse chatVoice(VoiceChatRequest request);
}


