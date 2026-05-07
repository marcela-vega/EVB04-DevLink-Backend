package com.DevLink.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApplicationResponse(
        Long id,
        Long projectId,
        Long applicantId,
        ApplicantInfo applicant,
        String message,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record ApplicantInfo(
            Long id,
            String name,
            String email,
            List<String> stack,
            String avatar,
            String bio,
            String githubUrl,
            String gitlabUrl
    ) {}
}
