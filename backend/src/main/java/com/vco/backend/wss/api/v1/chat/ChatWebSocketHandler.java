package com.vco.backend.wss.api.v1.chat;

import java.io.IOException;
import java.util.UUID;

import com.vco.backend.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String CONVERSATION_ID_ATTRIBUTE = "conversationId";

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final ChatService chatService;

    public ChatWebSocketHandler(ObjectMapper objectMapper, Validator validator, ChatService chatService) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String conversationId = UUID.randomUUID().toString();
        session.getAttributes().put(CONVERSATION_ID_ATTRIBUTE, conversationId);
        send(session, ChatMessages.system("How can I help you today?", conversationId));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatRequest request = objectMapper.readValue(message.getPayload(), ChatRequest.class);
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            send(session, validationError(session, violations.iterator().next()));
            return;
        }

        String conversationId = request.conversationId();
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = (String) session.getAttributes().get(CONVERSATION_ID_ATTRIBUTE);
        }

        try {
            String response = chatService.prompt(conversationId, request.content());
            send(session, ChatMessages.assistant(response, conversationId));
        }
        catch (RuntimeException ex) {
            log.error("Failed to process chat request for conversation {}", conversationId, ex);
            send(session, ChatMessages.error("Something went wrong while processing your request.", conversationId));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close(CloseStatus.SERVER_ERROR);
    }

    private ChatMessage validationError(WebSocketSession session, ConstraintViolation<ChatRequest> violation) {
        return ChatMessages.error(
                "Invalid request: " + violation.getMessage(),
                (String) session.getAttributes().get(CONVERSATION_ID_ATTRIBUTE)
        );
    }

    private void send(WebSocketSession session, ChatMessage message) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
    }
}
