package com.DevLink.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApplicationResponse(
        Long id,
        String status,
        LocalDateTime appliedAt,
        Long applicantId,
        String applicantName,
        String applicantEmail,
        String applicantBio,
        String githubUrl,
        String gitlabUrl,
        List<TechnologyResponse> technologies
) {}
