package com.DevLink.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordStep1Request(
        @NotBlank @Email String email
) {}
