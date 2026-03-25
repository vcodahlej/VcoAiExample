package com.vco.backend.wss.api.v1.chat;

import java.time.Instant;

public final class ChatMessages {

    private ChatMessages() {
    }

    public static ChatMessage assistant(String content, String conversationId) {
        return new ChatMessage("assistant", content, conversationId, Instant.now());
    }

    public static ChatMessage system(String content, String conversationId) {
        return new ChatMessage("system", content, conversationId, Instant.now());
    }

    public static ChatMessage error(String content, String conversationId) {
        return new ChatMessage("error", content, conversationId, Instant.now());
    }
}
