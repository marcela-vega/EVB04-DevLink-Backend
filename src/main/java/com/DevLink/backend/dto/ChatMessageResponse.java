package com.DevLink.backend.dto;

import com.DevLink.backend.entity.enums.MessageType;
import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        String content,
        MessageType type,
        String language,
        LocalDateTime createdAt
) {}