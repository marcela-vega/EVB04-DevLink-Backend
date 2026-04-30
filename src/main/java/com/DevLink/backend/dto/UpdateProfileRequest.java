package com.DevLink.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateProfileRequest(
        @Size(max = 150) String name,
        @Size(max = 2000) String bio,
        @Pattern(regexp = "^(https?://).*$", message = "githubUrl must be a valid URL") String githubUrl,
        @Pattern(regexp = "^(https?://).*$", message = "gitlabUrl must be a valid URL") String gitlabUrl,
        List<String> stack
) {}
