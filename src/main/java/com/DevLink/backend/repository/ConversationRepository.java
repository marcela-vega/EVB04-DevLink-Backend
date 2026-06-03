package com.DevLink.backend.repository;

import com.DevLink.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUserLow_IdAndUserHigh_Id(Long userLowId, Long userHighId);

    @Query("""
           select c from Conversation c
           where c.userLow.id = :userId or c.userHigh.id = :userId
           order by c.lastMessageAt desc
           """)
    List<Conversation> findAllForUser(@Param("userId") Long userId);
}