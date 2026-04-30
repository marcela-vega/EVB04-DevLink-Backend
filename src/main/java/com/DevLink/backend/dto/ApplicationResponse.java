package com.DevLink.backend.dto;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long projectId,
        ProjectResponse project,
        Long applicantId,
        UserProfileResponse applicant,
        String message,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
