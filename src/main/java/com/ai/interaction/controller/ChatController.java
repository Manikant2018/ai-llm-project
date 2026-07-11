package com.ai.interaction.controller;

import com.ai.interaction.beans.response.ChatResponse;
import com.ai.interaction.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chat(
            @RequestParam String message,
            @RequestParam String llm) {
        String response = chatService.chat(llm, message);
        return ResponseEntity.ok(ChatResponse.builder()
                .response(response)
                .llm(llm)
                .originalMessage(message)
                .timestamp(System.currentTimeMillis())
                .build());
    }
}