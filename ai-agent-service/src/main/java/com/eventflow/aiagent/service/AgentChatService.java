package com.eventflow.aiagent.service;

import com.eventflow.aiagent.exception.AgentUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AgentChatService {

    private static final Logger log = LoggerFactory.getLogger(AgentChatService.class);

    private final ChatClient chatClient;

    public AgentChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String message) {
        try {
            String reply = chatClient.prompt().user(message).call().content();
            return reply == null ? "" : reply;
        } catch (RuntimeException ex) {
            log.warn("Chat call failed: {}", ex.toString());
            throw new AgentUnavailableException(ex);
        }
    }
}
