package com.ai.interaction.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Conversation {
    private final String id;
    private final List<Map<String, Object>> history;

    public Conversation(String id) {
        this.id = id;
        this.history = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public List<Map<String, Object>> getHistory() {
        return history;
    }

    public void addMessage(Map<String, Object> message) {
        this.history.add(message);
    }
}
