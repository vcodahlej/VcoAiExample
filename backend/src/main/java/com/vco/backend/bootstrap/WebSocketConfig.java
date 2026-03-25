package com.vco.backend.bootstrap;

import com.vco.backend.wss.api.v1.chat.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final String allowedOrigin;

    public WebSocketConfig(
            ChatWebSocketHandler chatWebSocketHandler,
            @Value("${app.allowed-origin:http://localhost:5173}") String allowedOrigin
    ) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/wss/api/v1/chat")
                .setAllowedOrigins(allowedOrigin);
    }
}
