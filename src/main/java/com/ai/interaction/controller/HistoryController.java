package com.ai.interaction.controller;

import com.ai.interaction.model.Message;
import com.ai.interaction.repository.MessageRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final MessageRepository messageRepository;

    public HistoryController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/{interactionId}")
    public List<Message> getConversationHistory(@PathVariable String interactionId) {
        return messageRepository.findByInteractionIdOrderByTimestampAsc(interactionId);
    }
}
