package com.ai.interaction;

import org.springframework.ai.autoconfigure.anthropic.AnthropicAutoConfiguration;
import org.springframework.ai.autoconfigure.mistralai.MistralAiAutoConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

// Exclude specific Spring AI auto-configuration classes here
@SpringBootApplication(exclude = {
        OpenAiAutoConfiguration.class,
        MistralAiAutoConfiguration.class,
        AnthropicAutoConfiguration.class,
        VertexAiGeminiAutoConfiguration.class
})
@EnableAsync
public class InteractionApplication {

    public static void main(String[] args) {
        SpringApplication.run(InteractionApplication.class, args);
    }

}
