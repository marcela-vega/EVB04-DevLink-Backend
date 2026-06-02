package com.DevLink.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SecurityAnswerRequest(
        @NotNull Integer questionId,
        @NotBlank String answer
) {}
