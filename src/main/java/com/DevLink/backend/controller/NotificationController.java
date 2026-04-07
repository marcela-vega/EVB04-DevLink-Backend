package com.DevLink.backend.controller;

import com.DevLink.backend.dto.NotificationResponse;
import com.DevLink.backend.service.NotificationService;
import com.DevLink.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        Long userId = userService.getCurrentUserEntity(authentication.getName()).getId();
        return ResponseEntity.ok(notificationService.getForUser(userId));
    }
}
