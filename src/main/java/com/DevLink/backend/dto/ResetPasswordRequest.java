package com.DevLink.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotBlank String resetToken,
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must have at least 8 characters, one uppercase letter, and one number")
        String newPassword
) {}
