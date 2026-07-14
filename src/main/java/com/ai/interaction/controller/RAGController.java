package com.ai.interaction.controller;

import com.ai.interaction.service.RAGService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai/rag-impressive-interact")
public class RAGController {

    private final RAGService ragService;

    public RAGController(RAGService ragService) {
        this.ragService = ragService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<String>> ragImpressiveInteract(@RequestParam String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = ragService.ragQuery(message);
                return ResponseEntity.ok(response);
            } catch (Exception ex) {
                ex.printStackTrace();
                return ResponseEntity.internalServerError().body("Error processing impressive RAG query: " + ex.getMessage());
            }
        });
    }
}
