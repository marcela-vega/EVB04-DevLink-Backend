package com.DevLink.backend.dto;

import jakarta.validation.constraints.NotNull;

public record StartConversationRequest(@NotNull Long recipientId) {}