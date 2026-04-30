package com.DevLink.backend.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record RegisterRequest(
        // front sends "name", kept as alias via @JsonAlias in serialization — handled via getters below
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must have at least 8 characters, one uppercase letter, and one number")
        String password,
        // front sends "stack" as array of technology ID strings
        List<String> stack,
        @Size(max = 2000) String bio,
        @Pattern(regexp = "^(https?://).*$", message = "githubUrl must be a valid URL") String githubUrl,
        @Pattern(regexp = "^(https?://).*$", message = "gitlabUrl must be a valid URL") String gitlabUrl
) {}
