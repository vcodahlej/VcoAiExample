package com.vco.backend.wss.api.v1.chat;

import java.time.Instant;

public record ChatMessage(
        String type,
        String content,
        String conversationId,
        Instant timestamp
) {
}
