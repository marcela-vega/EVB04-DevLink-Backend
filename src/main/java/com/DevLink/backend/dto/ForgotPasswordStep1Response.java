package com.DevLink.backend.dto;

import java.util.List;

public record ForgotPasswordStep1Response(List<SecurityQuestionResponse> questions) {}
