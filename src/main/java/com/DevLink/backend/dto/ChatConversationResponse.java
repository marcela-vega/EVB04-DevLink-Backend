package com.DevLink.backend.dto;

import java.time.LocalDateTime;

public record ChatConversationResponse(
        Long id,
        ChatUserResponse otherUser,
        String lastMessage,
        LocalDateTime lastMessageAt
) {}