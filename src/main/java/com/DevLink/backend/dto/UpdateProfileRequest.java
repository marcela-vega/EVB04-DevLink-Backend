package com.DevLink.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 2000) String bio,
        @Pattern(regexp = "^(https?://).*$", message = "githubUrl must be a valid URL") String githubUrl,
        @Pattern(regexp = "^(https?://).*$", message = "gitlabUrl must be a valid URL") String gitlabUrl,
        List<Integer> technologyIds
) {}
