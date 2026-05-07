package com.DevLink.backend.service;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.entity.Role;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.exception.BadRequestException;
import com.DevLink.backend.exception.NotFoundException;
import com.DevLink.backend.repository.RoleRepository;
import com.DevLink.backend.repository.UserRepository;
import com.DevLink.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TechnologyService technologyService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MapperService mapperService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email is already registered");
        }

        Role developerRole = roleRepository.findByName("DEVELOPER")
                .orElseThrow(() -> new NotFoundException("Default role DEVELOPER not found"));

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .bio(trimToNull(request.bio()))
                .githubUrl(trimToNull(request.githubUrl()))
                .gitlabUrl(trimToNull(request.gitlabUrl()))
                .active(true)
                .roles(new HashSet<>(List.of(developerRole)))
                .technologies(new HashSet<>(technologyService.getTechnologiesByIds(request.technologyIds())))
                .build();

        User saved = userRepository.save(user);
        String token = generateToken(saved);
        String expiresAt = Instant.now().plusMillis(jwtService.getExpirationMs()).toString();
        return new AuthResponse(token, expiresAt, mapperService.toUserProfileResponse(saved));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String token = generateToken(user);
        String expiresAt = Instant.now().plusMillis(jwtService.getExpirationMs()).toString();
        return new AuthResponse(token, expiresAt, mapperService.toUserProfileResponse(user));
    }

    private String generateToken(User user) {
        org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getRoles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.getName()))
                        .toList()
        );

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles", user.getRoles().stream().map(Role::getName).toList());
        return jwtService.generateToken(principal, claims);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
