package com.ai.interaction.controller;

import com.ai.interaction.service.GeminiInteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/ai/tool-interact")
public class ToolInteractionController {

    private final GeminiInteractionService geminiInteractionService;

    public ToolInteractionController(GeminiInteractionService geminiInteractionService) {
        this.geminiInteractionService = geminiInteractionService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<Map<String, Object>>> toolInteract(
            @RequestParam String message,
            @RequestParam String interactionId) {
        return geminiInteractionService.toolInteract(message, interactionId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    // Log the exception
                    ex.printStackTrace();
                    return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()));
                });
    }
}
