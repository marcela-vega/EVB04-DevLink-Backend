package com.DevLink.backend.repository;

import com.DevLink.backend.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByConversation_IdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    Optional<ChatMessage> findTopByConversation_IdOrderByCreatedAtDesc(Long conversationId);
}