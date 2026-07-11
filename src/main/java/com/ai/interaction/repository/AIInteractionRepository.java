package com.ai.interaction.repository;

import com.ai.interaction.entity.AIInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIInteractionRepository extends JpaRepository<AIInteraction, Long> {
    List<AIInteraction> findByInteractionIdOrderByTimestampAsc(String interactionId);
}