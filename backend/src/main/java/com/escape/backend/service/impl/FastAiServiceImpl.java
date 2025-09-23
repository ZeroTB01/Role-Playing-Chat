package com.escape.backend.service.impl;

import com.escape.backend.service.FastAiService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FastAiServiceImpl implements FastAiService {
    @Override
    public Map<String, Object> callChat(String characterName, List<Map<String, String>> context) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Map<String, Object> callProcessAudio(String characterName, String audioBase64) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}


