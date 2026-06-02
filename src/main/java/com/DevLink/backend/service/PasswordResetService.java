package com.DevLink.backend.service;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.entity.PasswordResetToken;
import com.DevLink.backend.entity.SecurityQuestion;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.entity.UserSecurityAnswer;
import com.DevLink.backend.exception.BadRequestException;
import com.DevLink.backend.exception.NotFoundException;
import com.DevLink.backend.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final UserSecurityAnswerRepository userSecurityAnswerRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    // ── Step 0: set security answers (called after register) ──────────────────

    @Transactional
    public void setSecurityAnswers(String email, SetSecurityAnswersRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        long distinctQuestions = request.answers().stream()
                .map(SecurityAnswerRequest::questionId)
                .distinct()
                .count();
        if (distinctQuestions < request.answers().size()) {
            throw new BadRequestException("Debes elegir preguntas diferentes para cada respuesta");
        }

        // Delete existing answers and flush immediately so the INSERT doesn't hit the unique constraint
        userSecurityAnswerRepository.deleteByUserId(user.getId());
        entityManager.flush();

        for (SecurityAnswerRequest item : request.answers()) {
            SecurityQuestion question = securityQuestionRepository.findById(item.questionId())
                    .orElseThrow(() -> new NotFoundException("Question " + item.questionId() + " not found"));

            UserSecurityAnswer answer = UserSecurityAnswer.builder()
                    .user(user)
                    .question(question)
                    .answerHash(passwordEncoder.encode(normalise(item.answer())))
                    .build();
            userSecurityAnswerRepository.save(answer);
        }
    }

    @Transactional(readOnly = true)
    public boolean hasSecurityAnswers(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(u -> userSecurityAnswerRepository.existsByUserId(u.getId()))
                .orElse(false);
    }

    // ── Step 1: verify email, return user's security questions ────────────────

    @Transactional(readOnly = true)
    public ForgotPasswordStep1Response verifyEmail(ForgotPasswordStep1Request request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException("No account found with that email"));

        List<UserSecurityAnswer> answers = userSecurityAnswerRepository.findWithQuestionByUserId(user.getId());
        if (answers.isEmpty()) {
            throw new BadRequestException("Esta cuenta no tiene preguntas de seguridad configuradas");
        }

        List<SecurityQuestionResponse> questions = answers.stream()
                .map(a -> new SecurityQuestionResponse(a.getQuestion().getId(), a.getQuestion().getQuestion()))
                .toList();

        return new ForgotPasswordStep1Response(questions);
    }

    // ── Step 2: verify answers, return short-lived reset token ────────────────

    @Transactional
    public ForgotPasswordStep2Response verifyAnswers(ForgotPasswordStep2Request request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException("No account found with that email"));

        List<UserSecurityAnswer> storedAnswers = userSecurityAnswerRepository
                .findWithQuestionByUserId(user.getId());

        log.info("=== verifyAnswers: stored questions for user {} ===", user.getId());
        storedAnswers.forEach(a -> log.info("  stored questionId={} hash={}", a.getQuestion().getId(), a.getAnswerHash()));
        log.info("=== submitted answers ===");
        request.answers().forEach(a -> log.info("  submitted questionId={} answer='{}' normalised='{}'", a.questionId(), a.answer(), normalise(a.answer())));

        for (SecurityAnswerRequest submitted : request.answers()) {
            UserSecurityAnswer stored = storedAnswers.stream()
                    .filter(a -> a.getQuestion().getId().equals(submitted.questionId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Invalid question id: " + submitted.questionId()));

            boolean matches = passwordEncoder.matches(normalise(submitted.answer()), stored.getAnswerHash());
            log.info("  questionId={} matches={}", submitted.questionId(), matches);
            if (!matches) {
                throw new BadRequestException("Una o más respuestas son incorrectas");
            }
        }

        // invalidate previous tokens for this user
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        return new ForgotPasswordStep2Response(token);
    }

    // ── Step 3: reset password using the token ────────────────────────────────

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.resetToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.getUsed()) {
            throw new BadRequestException("Este enlace de recuperación ya fue utilizado");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El enlace de recuperación ha expirado");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    // ── Step 0: list available questions ─────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SecurityQuestionResponse> listQuestions() {
        return securityQuestionRepository.findAll().stream()
                .map(q -> new SecurityQuestionResponse(q.getId(), q.getQuestion()))
                .toList();
    }

    private String normalise(String value) {
        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
