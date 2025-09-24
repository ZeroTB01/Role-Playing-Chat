package com.escape.backend.service.impl;

import com.escape.backend.service.FastAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FastAiServiceImpl implements FastAiService {

    @Value("${fastapi.base-url:http://127.0.0.1:8000}")
    private String baseUrl;

    private WebClient createClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
                .build();
    }

    @Override
    public Map<String, Object> callChat(String characterName, List<Map<String, String>> context) {
        WebClient client = createClient();

        // 优先尝试：按约定的 { character, context } 协议
        Map<String, Object> payload = new HashMap<>();
        payload.put("character", characterName);
        payload.put("context", context);

        try {
            Map<String, Object> resp = client.post()
                    .uri("/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(payload))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (resp != null && (resp.containsKey("reply") || resp.containsKey("data"))) {
                return resp;
            }
        } catch (Exception ignored) {
        }

        // 兼容回退：FastAPI 现有实现 { role_id, message }
        // 若 context 非空，取最后一条 user 消息；否则用空串
        String message = context != null && !context.isEmpty() ? context.get(context.size() - 1).getOrDefault("content", "") : "";
        Map<String, Object> fallback = new HashMap<>();
        // 这里没有 role_id，只能让上层传入 characterId 作为最后一条 context 的 content 附带；
        // 为了兼容，尝试将 characterName 作为消息前缀。
        fallback.put("role_id", 0);
        fallback.put("message", (characterName != null ? ("[" + characterName + "] ") : "") + message);
        try {
            Map<String, Object> resp = client.post()
                    .uri("/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(fallback))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return resp;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    @Override
    public Map<String, Object> callProcessAudio(String characterName, String audioBase64) {
        WebClient client = createClient();

        // 优先尝试：约定的 { character, audioBase64 }
        Map<String, Object> payload = new HashMap<>();
        payload.put("character", characterName);
        payload.put("audioBase64", audioBase64);
        try {
            Map<String, Object> resp = client.post()
                    .uri("/process_audio")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(payload))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (resp != null && (resp.containsKey("audioBase64") || resp.containsKey("reply"))) {
                return resp;
            }
        } catch (Exception ignored) {
        }

        // 回退：现有 FastAPI /voice_chat 期待 multipart（role_id, file）
        try {
            // 这里没有真正的文件名与 role_id，简单回退：以 base64 文本作为字节内容
            byte[] audioBytes = audioBase64 != null ? audioBase64.getBytes(StandardCharsets.UTF_8) : new byte[0];
            Map<String, Object> resp = client.post()
                    .uri(uriBuilder -> uriBuilder.path("/voice_chat").queryParam("role_id", 0).build())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData("file", audioBytes))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return resp;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
}


