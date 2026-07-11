package com.ai.interaction.repository;

import com.ai.interaction.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByInteractionIdOrderByTimestampAsc(String interactionId);
}
