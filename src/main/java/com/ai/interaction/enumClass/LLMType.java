package com.ai.interaction.enumClass;

public enum LLMType {
    OPENAI("openai"),
    GEMINI("gemini"),
    GROQ("groq");
    private final String value;
    LLMType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}