package com.DevLink.backend.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateProjectRequest(
        @Size(max = 150) String title,
        @Size(max = 5000) String description,
        List<String> stackRequired
) {}
