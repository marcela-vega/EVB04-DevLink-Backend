package com.DevLink.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        Long id,
        String title,
        String description,
        String status,
        Long creatorId,
        String creatorName,
        List<TechnologyResponse> technologies,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean canApply
) {}
