package com.ai.interaction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String interactionId;

    @Column(nullable = false)
    private String role; // e.g., "user", "model"

    @Lob // For potentially long text content
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Default constructor for JPA
    public Message() {
    }

    public Message(String interactionId, String role, String content, LocalDateTime timestamp) {
        this.interactionId = interactionId;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(String interactionId) {
        this.interactionId = interactionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
               "id=" + id +
               ", interactionId='" + interactionId + '\'' +
               ", role='" + role + '\'' +
               ", content='" + content + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}
