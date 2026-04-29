package com.DevLink.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMessageRequest(
        @NotNull Long receiverId,
        @NotBlank @Size(max = 5000) String content
) {}
