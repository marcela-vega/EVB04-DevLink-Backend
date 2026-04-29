package com.DevLink.backend.dto;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long userId,
        String userName,
        String lastMessageContent,
        LocalDateTime lastMessageAt,
        long unreadCount
) {}
