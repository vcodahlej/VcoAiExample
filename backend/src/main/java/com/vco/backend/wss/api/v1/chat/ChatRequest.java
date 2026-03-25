package com.vco.backend.wss.api.v1.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        String type,
        String conversationId,
        @NotBlank String content
) {
}
