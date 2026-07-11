package com.ai.interaction.service;

import com.ai.interaction.model.Message;
import com.ai.interaction.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper; // Re-introducing ObjectMapper

    // Define the tools available to Gemini
    private static final List<Map<String, Object>> TOOL_DEFINITIONS = List.of(
            Map.of(
                    "function_declarations", List.of(
                            Map.of(
                                    "name", "getOrderStatus",
                                    "description", "Get the current status of a mail-order pharmacy prescription order.",
                                    "parameters", Map.of(
                                            "type", "OBJECT",
                                            "properties", Map.of(
                                                    "orderId", Map.of(
                                                            "type", "STRING",
                                                            "description", "The unique identifier for the order."
                                                    )
                                            ),
                                            "required", List.of("orderId")
                                    )
                            ),
                            Map.of(
                                    "name", "getMedicationInfo",
                                    "description", "Retrieve general information about a specific medication.",
                                    "parameters", Map.of(
                                            "type", "OBJECT",
                                            "properties", Map.of(
                                                    "medicationName", Map.of(
                                                            "type", "STRING",
                                                            "description", "The name of the medication."
                                                    )
                                            ),
                                            "required", List.of("medicationName")
                                    )
                            )
                    )
            )
    );


    public GeminiInteractionService(@Value("${gemini.api.key}") String apiKey,
                                   @Value("${gemini.model.name}") String modelName,
                                   MessageRepository messageRepository,
                                   ObjectMapper objectMapper) { // Inject ObjectMapper
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    // Existing interact method (unchanged)
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

    // NEW toolInteract method
    @Async
    public CompletableFuture<Map<String, Object>> toolInteract(String message, String interactionId) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.log(Level.INFO, "Received tool-enabled interaction request for interactionId: {0}, message: {1}", new Object[]{interactionId, message});

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
                String roleToSend = "user".equals(msg.getRole()) ? "user" : "model";
                geminiMessage.put("role", roleToSend);
                geminiMessage.put("parts", List.of(Map.of("text", msg.getContent())));
                geminiHistory.add(geminiMessage);
            }

            // --- First call to Gemini with user message and tool definitions ---
            Map<String, Object> body = new HashMap<>();
            body.put("contents", geminiHistory);
            body.put("tools", TOOL_DEFINITIONS); // Add tool definitions to the request
            LOGGER.log(Level.INFO, "Sending initial request body to Gemini API for interactionId {0}: {1}", new Object[]{interactionId, body});

            Map<String, Object> responseBody = callGeminiApi(body, interactionId);

            // --- Check for Function Call ---
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                        if (content.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            for (Map<String, Object> part : parts) {
                                if (part.containsKey("functionCall")) {
                                    Map<String, Object> functionCall = (Map<String, Object>) part.get("functionCall");
                                    String functionName = (String) functionCall.get("name");
                                    Map<String, Object> functionArgs = (Map<String, Object>) functionCall.get("args");

                                    LOGGER.log(Level.INFO, "Gemini requested function call: {0} with args: {1} for interactionId: {2}", new Object[]{functionName, functionArgs, interactionId});

                                    // Execute the function locally
                                    Object functionResult = executeToolFunction(functionName, functionArgs);

                                    // --- Second call to Gemini with tool result ---
                                    Map<String, Object> toolResponsePart = new HashMap<>();
                                    toolResponsePart.put("functionResponse", Map.of(
                                            "name", functionName,
                                            "response", Map.of("result", functionResult) // Wrap result in a "result" key
                                    ));

                                    // Add the tool call and its response to the history for Gemini
                                    geminiHistory.add(Map.of("role", "model", "parts", List.of(part))); // The function call itself
                                    geminiHistory.add(Map.of("role", "function", "parts", List.of(toolResponsePart))); // The function's response

                                    Map<String, Object> secondBody = new HashMap<>();
                                    secondBody.put("contents", geminiHistory);
                                    secondBody.put("tools", TOOL_DEFINITIONS); // Still provide tools
                                    LOGGER.log(Level.INFO, "Sending second request body (with tool result) to Gemini API for interactionId {0}: {1}", new Object[]{interactionId, secondBody});

                                    responseBody = callGeminiApi(secondBody, interactionId);
                                    // After the second call, the responseBody should contain Gemini's final text response
                                    break; // Assuming only one function call per turn for simplicity
                                }
                            }
                        }
                    }
                }
            }

            // Process and save Gemini's final response (text or tool output)
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        Map<String, Object> geminiResponseContent = (Map<String, Object>) candidate.get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) geminiResponseContent.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            // Check if the response is text or another function call (should be text after tool execution)
                            for (Map<String, Object> part : parts) {
                                if (part.containsKey("text")) {
                                    String geminiText = (String) part.get("text");
                                    Message geminiDbMessage = new Message(interactionId, "model", geminiText, LocalDateTime.now());
                                    messageRepository.save(geminiDbMessage);
                                    break; // Save the first text response
                                }
                            }
                        }
                    }
                }
            } else if (responseBody == null) {
                LOGGER.log(Level.SEVERE, "Failed to get a response from Gemini API after {0} retries for interactionId: {1}. No response body received.", new Object[]{MAX_RETRIES, interactionId});
            } else {
                LOGGER.log(Level.WARNING, "Gemini API response for interactionId {0} did not contain expected 'candidates' field or text content: {1}", new Object[]{interactionId, responseBody});
            }
            return responseBody;
        });
    }

    private Map<String, Object> callGeminiApi(Map<String, Object> requestBody, String interactionId) {
        Map<String, Object> responseBody = null;
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                responseBody = restClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/models/" + modelName + ":generateContent")
                                .queryParam("key", apiKey)
                                .build())
                        .body(requestBody)
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
        return responseBody;
    }

    // --- Local Tool Execution Methods ---
    private Object executeToolFunction(String functionName, Map<String, Object> args) {
        switch (functionName) {
            case "getOrderStatus":
                return getOrderStatus((String) args.get("orderId"));
            case "getMedicationInfo":
                return getMedicationInfo((String) args.get("medicationName"));
            default:
                LOGGER.log(Level.WARNING, "Unknown function call requested by Gemini: {0}", functionName);
                return Map.of("error", "Unknown function: " + functionName);
        }
    }

    private Map<String, String> getOrderStatus(String orderId) {
        // Simulate fetching order status from an internal system
        LOGGER.log(Level.INFO, "Executing getOrderStatus for orderId: {0}", orderId);
        if ("ORDER123".equals(orderId)) {
            return Map.of(
                    "orderId", orderId,
                    "status", "Shipped",
                    "estimatedDelivery", "2026-07-20",
                    "trackingNumber", "TRK789"
            );
        } else if ("ORDER456".equals(orderId)) {
            return Map.of(
                    "orderId", orderId,
                    "status", "Processing",
                    "estimatedDelivery", "2026-07-25"
            );
        } else {
            return Map.of(
                    "orderId", orderId,
                    "status", "Not Found",
                    "message", "Order with ID " + orderId + " could not be found."
            );
        }
    }

    private Map<String, String> getMedicationInfo(String medicationName) {
        // Simulate fetching medication info from an internal knowledge base
        LOGGER.log(Level.INFO, "Executing getMedicationInfo for medication: {0}", medicationName);
        if ("ibuprofen".equalsIgnoreCase(medicationName)) {
            return Map.of(
                    "medicationName", "Ibuprofen",
                    "description", "A nonsteroidal anti-inflammatory drug (NSAID) used for pain relief, fever reduction, and inflammation.",
                    "commonSideEffects", "Stomach upset, nausea, headache."
            );
        } else if ("amoxicillin".equalsIgnoreCase(medicationName)) {
            return Map.of(
                    "medicationName", "Amoxicillin",
                    "description", "A penicillin antibiotic used to treat a variety of bacterial infections.",
                    "commonSideEffects", "Nausea, diarrhea, rash."
            );
        } else {
            return Map.of(
                    "medicationName", medicationName,
                    "description", "Information not found for " + medicationName + ". Please consult a pharmacist.",
                    "commonSideEffects", "N/A"
            );
        }
    }
}
