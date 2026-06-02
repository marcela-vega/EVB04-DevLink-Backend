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
        CreatorInfo creator,
        List<CollaboratorInfo> collaborators,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String startedAt,
        String completedAt,
        long applicationCount,
        boolean canApply,
        Long currentUserApplicationId
) {
    public record CreatorInfo(Long id, String name, String avatar) {}
    public record CollaboratorInfo(Long id, String name, String avatar) {}
}
