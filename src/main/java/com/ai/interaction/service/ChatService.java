package com.ai.interaction.service;

import com.ai.interaction.enumClass.LLMType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient openAIChatClient;
    private final ChatClient geminiChatClient;
    private final ChatClient groqChatClient;

    @Autowired
    public ChatService(OpenAiChatModel openAiChatModel,
                       @Qualifier("geminiChatClient") ChatClient geminiChatClient,
                       @Qualifier("groqChatClient") ChatClient groqChatClient) {
        this.openAIChatClient = ChatClient.create(openAiChatModel);
        this.geminiChatClient = geminiChatClient;
        this.groqChatClient = groqChatClient;
    }

    public String chat(String llmName, String message) {
        var chatClient = getChatModel(LLMType.valueOf(llmName.toUpperCase()));
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    private ChatClient getChatModel(LLMType llmName) {
        return switch (llmName) {
            case OPENAI -> openAIChatClient;
            case GEMINI -> geminiChatClient;
            case GROQ -> groqChatClient;
        };
    }
}