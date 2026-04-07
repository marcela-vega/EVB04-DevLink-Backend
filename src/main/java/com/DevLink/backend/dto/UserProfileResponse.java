package com.DevLink.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String bio,
        String githubUrl,
        String gitlabUrl,
        boolean active,
        List<String> roles,
        List<TechnologyResponse> technologies,
        LocalDateTime createdAt
) {}
