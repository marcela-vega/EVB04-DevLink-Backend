package com.DevLink.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        Long id,
        String title,
        String description,
        List<String> stackRequired,
        String status,
        Long creatorId,
        UserProfileResponse creator,
        List<UserProfileResponse> collaborators,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        int applicationCount,
        boolean canApply
) {}
