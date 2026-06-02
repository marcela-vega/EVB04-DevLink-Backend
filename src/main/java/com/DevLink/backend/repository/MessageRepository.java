package com.DevLink.backend.repository;

import com.DevLink.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversation_IdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    Optional<Message> findTopByConversation_IdOrderByCreatedAtDesc(Long conversationId);
}