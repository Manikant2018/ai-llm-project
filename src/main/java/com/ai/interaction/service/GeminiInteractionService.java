package com.ai.interaction.service;

import com.ai.interaction.model.Message;
import com.ai.interaction.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class GeminiInteractionService {

    private static final Logger LOGGER = Logger.getLogger(GeminiInteractionService.class.getName());
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1 second

    private final RestClient restClient;
    private final String apiKey;
    private final String modelName;
    private final MessageRepository messageRepository;

    public GeminiInteractionService(@Value("${gemini.api.key}") String apiKey,
                                   @Value("${gemini.model.name}") String modelName,
                                   MessageRepository messageRepository) {
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
        this.messageRepository = messageRepository;
    }

    @Async
    public CompletableFuture<Map<String, Object>> interact(String message, String interactionId) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.log(Level.INFO, "Received interaction request for interactionId: {0}, message: {1}", new Object[]{interactionId, message});

            // Input Validation
            if (message == null || message.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Input message is null or empty for interactionId: {0}", interactionId);
                throw new IllegalArgumentException("Message cannot be null or empty.");
            }
            if (interactionId == null || interactionId.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Input interactionId is null or empty.");
                throw new IllegalArgumentException("Interaction ID cannot be null or empty.");
            }

            // Save user message to DB
            Message userDbMessage = new Message(interactionId, "user", message, LocalDateTime.now());
            messageRepository.save(userDbMessage);

            // Retrieve conversation history from DB
            List<Message> dbHistory = messageRepository.findByInteractionIdOrderByTimestampAsc(interactionId);
            List<Map<String, Object>> geminiHistory = new ArrayList<>();
            for (Message msg : dbHistory) {
                Map<String, Object> geminiMessage = new HashMap<>();
                // Ensure role is either "user" or "model" for Gemini API
                String roleToSend = "user".equals(msg.getRole()) ? "user" : "model";
                geminiMessage.put("role", roleToSend);
                geminiMessage.put("parts", List.of(Map.of("text", msg.getContent())));
                geminiHistory.add(geminiMessage);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("contents", geminiHistory);
            LOGGER.log(Level.INFO, "Sending request body to Gemini API for interactionId {0}: {1}", new Object[]{interactionId, body});

            Map<String, Object> responseBody = null;
            int retries = 0;
            while (retries < MAX_RETRIES) {
                try {
                    responseBody = restClient.post()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/models/" + modelName + ":generateContent")
                                    .queryParam("key", apiKey)
                                    .build())
                            .body(body)
                            .retrieve()
                            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
                    LOGGER.log(Level.INFO, "Received response from Gemini API for interactionId {0}: {1}", new Object[]{interactionId, responseBody});
                    break; // Success, exit retry loop
                } catch (RestClientException e) {
                    LOGGER.log(Level.WARNING, "Error interacting with Gemini API for interactionId {0}. Retry attempt {1}/{2}. Error: {3}", new Object[]{interactionId, (retries + 1), MAX_RETRIES, e.getMessage()});
                    retries++;
                    if (retries < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            LOGGER.log(Level.SEVERE, "Retry delay interrupted for interactionId: {0}", interactionId);
                            break; // Exit if interrupted
                        }
                    }
                }
            }

            // Save Gemini's response to DB if successful
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        Map<String, Object> geminiResponseContent = (Map<String, Object>) candidate.get("content");
                        // Ensure the role saved to DB is "model" for Gemini's response
                        String role = "model"; // Explicitly set to "model"
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) geminiResponseContent.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            String geminiText = (String) parts.get(0).get("text");
                            Message geminiDbMessage = new Message(interactionId, role, geminiText, LocalDateTime.now());
                            messageRepository.save(geminiDbMessage);
                        }
                    }
                }
            } else if (responseBody == null) {
                LOGGER.log(Level.SEVERE, "Failed to get a response from Gemini API after {0} retries for interactionId: {1}. No response body received.", new Object[]{MAX_RETRIES, interactionId});
                // Optionally, you might want to add a default error message to the conversation or throw a custom exception
            } else {
                LOGGER.log(Level.WARNING, "Gemini API response for interactionId {0} did not contain expected 'candidates' field: {1}", new Object[]{interactionId, responseBody});
            }
            return responseBody;
        });
    }
}