
package com.eventflow.aiagent.integration;

import com.eventflow.aiagent.controller.rest.ChatRestController;
import com.eventflow.aiagent.exception.AgentUnavailableException;
import com.eventflow.aiagent.exception.GlobalExceptionHandler;
import com.eventflow.aiagent.service.AgentChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatApiErrorHandlingTest {

    private MockMvc mockMvc;
    private AgentChatService chatService;

    @BeforeEach
    void setUp() {

        chatService = mock(AgentChatService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ChatRestController(chatService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnServiceUnavailableWhenAgentFails() throws Exception {

        when(chatService.chat(anyString()))
                .thenThrow(new AgentUnavailableException(
                        new RuntimeException("Groq API unavailable")
                ));

        mockMvc.perform(post("/api/agent/chat")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                    "message": "Hello"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail")
                        .value("The assistant is unavailable right now, try again later"));
    }
}
