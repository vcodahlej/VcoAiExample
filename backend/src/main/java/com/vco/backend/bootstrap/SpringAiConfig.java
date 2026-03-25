package com.vco.backend.bootstrap;

import com.vco.backend.service.tools.CommonTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SpringAiConfig {

    private final ChatModel chatModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final CommonTools commonTools;
    private final ToolCallbackProvider toolCallbackProvider;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(50)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    @Bean
    public ChatClient chatClient() {
       return  ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .defaultTools(commonTools)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }
}
