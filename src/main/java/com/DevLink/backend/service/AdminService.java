package com.DevLink.backend.service;

import com.DevLink.backend.dto.UserProfileResponse;
import com.DevLink.backend.entity.Role;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.exception.BadRequestException;
import com.DevLink.backend.exception.NotFoundException;
import com.DevLink.backend.repository.RoleRepository;
import com.DevLink.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MapperService mapperService;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<UserProfileResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .map(mapperService::toUserProfileResponse)
                .toList();
    }

    @Transactional
    public UserProfileResponse updateUserRole(Long userId, String roleName) {
        // Solo ADMIN puede cambiar roles (refuerzo en servicio además del @PreAuthorize)
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Rol no encontrado: " + roleName));
        // No se puede asignar rol ADMIN desde el panel (solo desde DB directamente)
        if ("ADMIN".equals(roleName.toUpperCase())) {
            throw new BadRequestException("No se puede asignar el rol de administrador desde el panel.");
        }
        entityManager.createNativeQuery("DELETE FROM user_roles WHERE user_id = :uid")
                .setParameter("uid", userId)
                .executeUpdate();
        entityManager.createNativeQuery("INSERT INTO user_roles (user_id, role_id) VALUES (:uid, :rid)")
                .setParameter("uid", userId)
                .setParameter("rid", role.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        return mapperService.toUserProfileResponse(getUser(userId));
    }

    @Transactional
    public UserProfileResponse setUserActive(Long userId, boolean active) {
        User target = getUser(userId);
        String targetRole = target.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("DEVELOPER");

        // Moderadores solo pueden suspender/reactivar developers
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean callerIsAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!callerIsAdmin && !"DEVELOPER".equals(targetRole)) {
            throw new BadRequestException("Los moderadores solo pueden suspender cuentas de desarrolladores.");
        }

        target.setActive(active);
        userRepository.saveAndFlush(target);
        entityManager.refresh(target);
        return mapperService.toUserProfileResponse(target);
    }

    private User getUser(Long userId) {
        return userRepository.findWithRolesAndTechnologiesById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
