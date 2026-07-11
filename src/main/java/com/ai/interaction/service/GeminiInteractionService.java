package com.ai.interaction.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class GeminiInteractionService {

    private final RestClient restClient;
    private final String apiKey;
    private final String modelName;

    public GeminiInteractionService(@Value("${gemini.api.key}") String apiKey,
                                   @Value("${gemini.model.name}") String modelName) {
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public Map<String, Object> interact(String message, String interactionId) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "models/" + modelName);
        body.put("input", message);

        if (interactionId != null && !interactionId.isEmpty()) {
            body.put("previous_interaction_id", interactionId);
        }

        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/interactions")
                        .queryParam("key", apiKey)
                        .build())
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}