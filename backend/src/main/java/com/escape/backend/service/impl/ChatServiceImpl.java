package com.escape.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.escape.backend.dto.ApiResponse;
import com.escape.backend.dto.TextChatRequest;
import com.escape.backend.dto.TextChatResponse;
import com.escape.backend.dto.VoiceChatRequest;
import com.escape.backend.dto.VoiceChatResponse;
import com.escape.backend.entity.Character;
import com.escape.backend.entity.ChatRecord;
import com.escape.backend.mapper.ChatRecordMapper;
import com.escape.backend.service.CharacterService;
import com.escape.backend.service.ChatService;
import com.escape.backend.service.FastAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {
    private static final int CONTEXT_LIMIT = 6;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private ChatRecordMapper chatRecordMapper;

    @Autowired
    private FastAiService fastAiService;

    @Value("${fastapi.mock:false}")
    private boolean mockMode;

    @Override
    public TextChatResponse chatText(TextChatRequest request) {
        // 1. 获取角色名
        Character character = characterService.getById(request.getCharacterId());
        String characterName = character != null ? character.getName() : "";

        // 2. 查询最近 N 条上下文（按时间倒序取，再反转），用 Map 避免依赖 Lombok getter
        QueryWrapper<ChatRecord> qw = new QueryWrapper<ChatRecord>()
                .eq("session_id", request.getCharacterId()) // 简化：暂以 characterId 代替 sessionId
                .orderByDesc("created_at")
                .last("limit " + CONTEXT_LIMIT);
        List<Map<String, Object>> latest = chatRecordMapper.selectMaps(qw);
        List<Map<String, String>> context = new ArrayList<>();
        for (int i = latest.size() - 1; i >= 0; i--) {
            Map<String, Object> row = latest.get(i);
            Map<String, String> item = new HashMap<>();
            Object role = row.get("role");
            Object content = row.get("content_text");
            item.put("role", role != null ? String.valueOf(role) : "user");
            item.put("content", content != null ? String.valueOf(content) : "");
            context.add(item);
        }
        // 追加当前用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", request.getMessage());
        context.add(userMsg);

        // 3. 调用 FastAPI 或 Mock
        String reply;
        if (mockMode) {
            reply = "[MOCK] " + characterName + "：你好，我已经收到你的消息——" + request.getMessage();
        } else {
            Map<String, Object> result = fastAiService.callChat(characterName, context);
            reply = result != null && result.get("reply") != null ? String.valueOf(result.get("reply")) : "";
        }

        // 4. 构造响应（落库先略）
        return new TextChatResponse(reply);
    }

    @Override
    public VoiceChatResponse chatVoice(VoiceChatRequest request) {
        Character character = characterService.getById(request.getCharacterId());
        String characterName = character != null ? character.getName() : "";
        VoiceChatResponse resp = new VoiceChatResponse();
        if (mockMode) {
            resp.setText("[MOCK-ASR] 语音转写内容");
            resp.setReply("[MOCK-LLM] 基于 " + characterName + " 的风格回复");
            resp.setAudioBase64("MOCK_AUDIO_BASE64");
            return resp;
        } else {
            Map<String, Object> result = fastAiService.callProcessAudio(characterName, request.getAudioBase64());
            if (result != null) {
                Object text = result.get("text");
                Object reply = result.get("reply");
                Object audio = result.get("audioBase64");
                resp.setText(text != null ? String.valueOf(text) : null);
                resp.setReply(reply != null ? String.valueOf(reply) : null);
                resp.setAudioBase64(audio != null ? String.valueOf(audio) : null);
            }
            return resp;
        }
    }
}


