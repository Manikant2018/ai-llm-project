package com.ai.interaction.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RAGConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {


        // SimpleVectorStore is an in-memory VectorStore suitable for demonstration.
        // It requires an EmbeddingModel to convert text to vectors.

        return SimpleVectorStore.builder(embeddingModel).build();
     }
}

