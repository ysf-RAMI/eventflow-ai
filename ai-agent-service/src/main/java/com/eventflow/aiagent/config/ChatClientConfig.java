package com.eventflow.aiagent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    static final String SYSTEM_PROMPT = """
            You are the EventFlow AI assistant for an event-booking platform.
            Answer briefly and politely.
            In this version you have no access to live event, seat or booking data, and you cannot
            book seats, pay or change anything. If asked about a specific event, seat availability
            or a booking, say that you cannot check that yet. Never claim to have done an action.
            """;

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(SYSTEM_PROMPT).build();
    }
}
