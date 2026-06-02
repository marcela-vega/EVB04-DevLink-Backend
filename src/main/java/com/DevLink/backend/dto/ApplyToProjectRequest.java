package com.DevLink.backend.dto;

import jakarta.validation.constraints.Size;

public record ApplyToProjectRequest(
        @Size(max = 1000) String message
) {}
