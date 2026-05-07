package com.DevLink.backend.dto;

public record AuthResponse(
        String token,
        String expiresAt,
        UserProfileResponse user
) {}
