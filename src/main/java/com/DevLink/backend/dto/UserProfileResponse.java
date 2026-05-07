package com.DevLink.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String role,
        String status,
        List<String> stack,
        String bio,
        String avatar,
        String githubUrl,
        String gitlabUrl,
        long projectsCount,
        long collaborationsCount,
        LocalDateTime createdAt
) {}
