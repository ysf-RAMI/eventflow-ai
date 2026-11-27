package com.eventflow.aiagent.controller.rest;

import com.eventflow.aiagent.dto.ChatRequest;
import com.eventflow.aiagent.dto.ChatResponse;
import com.eventflow.aiagent.service.AgentChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class ChatRestController {

    private final AgentChatService chatService;

    public ChatRestController(AgentChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(chatService.chat(request.message()));
    }
}
