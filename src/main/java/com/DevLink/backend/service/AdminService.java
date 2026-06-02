package com.DevLink.backend.service;

import com.DevLink.backend.dto.UserProfileResponse;
import com.DevLink.backend.entity.Role;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.exception.NotFoundException;
import com.DevLink.backend.repository.RoleRepository;
import com.DevLink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MapperService mapperService;

    @Transactional(readOnly = true)
    public List<UserProfileResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .map(mapperService::toUserProfileResponse)
                .toList();
    }

    @Transactional
    public UserProfileResponse updateUserRole(Long userId, String roleName) {
        User user = getUser(userId);
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
        user.setRoles(Set.of(role));
        userRepository.save(user);
        return mapperService.toUserProfileResponse(getUser(userId));
    }

    @Transactional
    public UserProfileResponse setUserActive(Long userId, boolean active) {
        User user = getUser(userId);
        user.setActive(active);
        userRepository.save(user);
        return mapperService.toUserProfileResponse(getUser(userId));
    }

    private User getUser(Long userId) {
        return userRepository.findWithRolesAndTechnologiesById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
