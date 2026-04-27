package com.DevLink.backend.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long senderId,
        String senderName,
        Long receiverId,
        String receiverName,
        String content,
        boolean isRead,
        LocalDateTime createdAt
) {}
