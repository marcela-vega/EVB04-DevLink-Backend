package com.DevLink.backend.dto;

public record AuthResponse(
        String token,
        UserProfileResponse user
) {}
