package com.ai.interaction.controller;

import com.ai.interaction.entity.AIInteraction;
import com.ai.interaction.repository.AIInteractionRepository;
import com.ai.interaction.service.GeminiInteractionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Advanced AI Orchestrator Controller for Gemini Interactions.
 * Includes Multimodal Support, Resilience (Circuit Breaker), and Observability.
 */
@RestController
@RequestMapping("/ai/gemini")
public class GeminiInteractionController {

    private final GeminiInteractionService interactionService;
    private final AIInteractionRepository repository;

    public GeminiInteractionController(GeminiInteractionService interactionService, AIInteractionRepository repository) {
        this.interactionService = interactionService;
        this.repository = repository;
    }

    /**
     * Standard interaction endpoint with Contextual ID and Multimodal (File) support.
     */
    @PostMapping("/interact")
    public Map<String, Object> interact(
            @RequestParam String message,
            @RequestParam(required = false) String interactionId,
            @RequestParam(required = false) MultipartFile file) {
        return interactionService.interact(message, interactionId);
    }


    /**
     * History endpoint to retrieve persisted conversations from the H2 database.
     */
    @GetMapping("/history")
    public List<AIInteraction> getHistory(@RequestParam String interactionId) {
        return repository.findByInteractionIdOrderByTimestampAsc(interactionId);
    }
}