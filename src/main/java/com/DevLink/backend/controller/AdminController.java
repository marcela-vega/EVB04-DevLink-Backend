package com.DevLink.backend.controller;

import com.DevLink.backend.dto.UserProfileResponse;
import com.DevLink.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listAllUsers());
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.updateUserRole(id, body.get("role")));
    }

    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<UserProfileResponse> suspendUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.setUserActive(id, false));
    }

    @PostMapping("/users/{id}/reactivate")
    public ResponseEntity<UserProfileResponse> reactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.setUserActive(id, true));
    }
}
