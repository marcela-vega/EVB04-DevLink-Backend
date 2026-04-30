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
        LocalDateTime createdAt,
        int projectsCount,
        int collaborationsCount,
        String githubUrl,
        String gitlabUrl
) {}
