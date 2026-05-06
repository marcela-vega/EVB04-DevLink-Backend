package com.DevLink.backend.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        boolean read,
        String link,
        LocalDateTime createdAt
) {}
