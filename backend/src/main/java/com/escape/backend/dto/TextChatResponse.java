package com.escape.backend.dto;

public class TextChatResponse {
    private String reply;

    public TextChatResponse() {
    }

    public TextChatResponse(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}


