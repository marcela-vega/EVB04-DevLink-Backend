package com.DevLink.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ForgotPasswordStep2Request(
        @NotBlank @Email String email,
        @NotEmpty @Valid List<SecurityAnswerRequest> answers
) {}
