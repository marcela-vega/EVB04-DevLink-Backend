package com.DevLink.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SetSecurityAnswersRequest(
        @NotEmpty @Size(min = 2, max = 2) @Valid List<SecurityAnswerRequest> answers
) {}
