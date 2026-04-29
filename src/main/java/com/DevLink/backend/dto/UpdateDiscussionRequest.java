package com.DevLink.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateDiscussionRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 10000) String content,
        @NotEmpty List<Integer> technologyIds
) {}
