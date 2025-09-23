package com.escape.backend.service;

import java.util.List;
import java.util.Map;

public interface FastAiService {
    Map<String, Object> callChat(String characterName, List<Map<String, String>> context);
    Map<String, Object> callProcessAudio(String characterName, String audioBase64);
}


