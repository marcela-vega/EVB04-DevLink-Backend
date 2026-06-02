package com.DevLink.backend.controller;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /** Public: list all available security questions (for registration UI) */
    @GetMapping("/security-questions")
    public ResponseEntity<List<SecurityQuestionResponse>> listQuestions() {
        return ResponseEntity.ok(passwordResetService.listQuestions());
    }

    /** Authenticated: check if user already has security answers */
    @GetMapping("/security-answers/status")
    public ResponseEntity<java.util.Map<String, Boolean>> getAnswersStatus(Authentication authentication) {
        boolean set = passwordResetService.hasSecurityAnswers(authentication.getName());
        return ResponseEntity.ok(java.util.Map.of("set", set));
    }

    /** Authenticated: save answers for the logged-in user */
    @PostMapping("/security-answers")
    public ResponseEntity<Void> setAnswers(
            Authentication authentication,
            @Valid @RequestBody SetSecurityAnswersRequest request) {
        passwordResetService.setSecurityAnswers(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    /** Public step 1: verify email exists, return user's questions */
    @PostMapping("/forgot-password/verify-email")
    public ResponseEntity<ForgotPasswordStep1Response> verifyEmail(
            @Valid @RequestBody ForgotPasswordStep1Request request) {
        return ResponseEntity.ok(passwordResetService.verifyEmail(request));
    }

    /** Public step 2: verify answers, return short-lived reset token */
    @PostMapping("/forgot-password/verify-answers")
    public ResponseEntity<ForgotPasswordStep2Response> verifyAnswers(
            @Valid @RequestBody ForgotPasswordStep2Request request) {
        log.info("=== verify-answers received: email={} answers={}", request.email(), request.answers());
        return ResponseEntity.ok(passwordResetService.verifyAnswers(request));
    }

    /** Public step 3: set new password using the reset token */
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
