package com.DevLink.backend.service;

import com.DevLink.backend.dto.UpdateProfileRequest;
import com.DevLink.backend.dto.UserProfileResponse;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.entity.enums.NotificationType;
import com.DevLink.backend.exception.NotFoundException;
import com.DevLink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TechnologyService technologyService;
    private final MapperService mapperService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public User getCurrentUserEntity(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(String email) {
        return mapperService.toUserProfileResponse(getCurrentUserEntity(email));
    }

    @Transactional(readOnly = true)
    public User getPublicUserEntity(Long userId) {
        return userRepository.findWithRolesAndTechnologiesById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfile(Long userId) {
        return mapperService.toUserProfileResponse(getPublicUserEntity(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getCurrentUserEntity(email);
        user.setFullName(request.fullName().trim());
        user.setBio(trimToNull(request.bio()));
        user.setGithubUrl(trimToNull(request.githubUrl()));
        user.setGitlabUrl(trimToNull(request.gitlabUrl()));
        user.setTechnologies(new HashSet<>(technologyService.getTechnologiesByIds(request.technologyIds())));

        User saved = userRepository.save(user);
        notificationService.create(saved,
                "Profile updated",
                "Your technical profile was updated successfully.",
                NotificationType.PROFILE_UPDATED);
        return mapperService.toUserProfileResponse(saved);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
