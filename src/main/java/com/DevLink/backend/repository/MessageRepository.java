package com.DevLink.backend.repository;

import com.DevLink.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
            SELECT m FROM Message m
            JOIN FETCH m.sender JOIN FETCH m.receiver
            WHERE (m.sender.id = :userId AND m.receiver.id = :otherId)
               OR (m.sender.id = :otherId AND m.receiver.id = :userId)
            ORDER BY m.createdAt DESC
            """)
    Page<Message> findConversation(@Param("userId") Long userId, @Param("otherId") Long otherId, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT ON (partner_id) sub.*
            FROM (
                SELECT m.*, CASE WHEN m.sender_id = :userId THEN m.receiver_id ELSE m.sender_id END AS partner_id
                FROM messages m
                WHERE m.sender_id = :userId OR m.receiver_id = :userId
            ) sub
            ORDER BY partner_id, sub.created_at DESC
            """, nativeQuery = true)
    List<Message> findLatestMessagePerConversation(@Param("userId") Long userId);

    long countByReceiverIdAndSenderIdAndIsReadFalse(Long receiverId, Long senderId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.isRead = false")
    void markConversationAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
}
