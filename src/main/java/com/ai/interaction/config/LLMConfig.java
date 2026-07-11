package com.ai.interaction.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Bean
    @Qualifier("geminiChatClient")
    public ChatClient geminiChatClient(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.url}") String geminiUrl,
            @Value("${gemini.api.completions.path}") String completionsPath,
            @Value("${gemini.model.name}") String modelName) {

        var geminiApi = OpenAiApi.builder()
                .baseUrl(geminiUrl)
                .completionsPath(completionsPath)
                .apiKey(apiKey)
                .build();

        var geminiModel = OpenAiChatModel.builder()
                .openAiApi(geminiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(modelName)
                        .build())
                .build();

        return ChatClient.builder(geminiModel).build();
    }

    @Bean
    @Qualifier("groqChatClient")
    public ChatClient groqChatClient(
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.api.url}") String groqUrl,
            @Value("${groq.api.completions.path}") String completionsPath,
            @Value("${groq.model.chat}") String modelName) {

        var groqApi = OpenAiApi.builder()
                .baseUrl(groqUrl)
                .completionsPath(completionsPath)
                .apiKey(apiKey)
                .build();

        var groqModel = OpenAiChatModel.builder()
                .openAiApi(groqApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(modelName)
                        .build())
                .build();

        return ChatClient.builder(groqModel).build();
    }

    @Bean
    @Qualifier("groqTranscriptionModel")
    public OpenAiAudioTranscriptionModel groqTranscriptionModel(
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.api.url}") String groqUrl,
            @Value("${groq.model.whisper}") String whisperModel) {

        var groqAudioApi = OpenAiAudioApi.builder()
                .baseUrl(groqUrl)
                .apiKey(apiKey)
                .build();

        return new OpenAiAudioTranscriptionModel(groqAudioApi,
                OpenAiAudioTranscriptionOptions.builder()
                        .model(whisperModel)
                        .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.JSON)
                        .build());
    }

    @Bean
    @Qualifier("groqSpeechModel")
    public OpenAiAudioSpeechModel groqSpeechModel(
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.api.url}") String groqUrl,
            @Value("${groq.model.tts}") String ttsModel) {

        var groqAudioApi = OpenAiAudioApi.builder()
                .baseUrl(groqUrl)
                .apiKey(apiKey)
                .build();

        return new OpenAiAudioSpeechModel(groqAudioApi,
                OpenAiAudioSpeechOptions.builder()
                        .model(ttsModel)
                        .build());
    }
}