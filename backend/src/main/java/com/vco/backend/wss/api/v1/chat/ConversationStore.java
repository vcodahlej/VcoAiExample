package com.vco.backend.wss.api.v1.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

@Component
public class ConversationStore {

    private final ConcurrentMap<String, List<Message>> conversations = new ConcurrentHashMap<>();

    public List<Message> getOrCreate(String conversationId) {
        return conversations.computeIfAbsent(conversationId, ignored -> new ArrayList<>());
    }
}
