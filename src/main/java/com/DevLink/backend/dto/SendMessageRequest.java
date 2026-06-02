package com.DevLink.backend.dto;

import com.DevLink.backend.entity.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(
        @NotNull Long conversationId,
        @NotBlank String content,
        @NotNull MessageType type,
        String language
) {}