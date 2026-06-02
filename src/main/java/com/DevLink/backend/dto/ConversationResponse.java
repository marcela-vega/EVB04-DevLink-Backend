package com.DevLink.backend.dto;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        ChatUserResponse otherUser,
        String lastMessage,
        LocalDateTime lastMessageAt
) {}