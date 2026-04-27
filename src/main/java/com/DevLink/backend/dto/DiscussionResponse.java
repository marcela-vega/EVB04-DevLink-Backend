package com.DevLink.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DiscussionResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorName,
        List<TechnologyResponse> technologies,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
