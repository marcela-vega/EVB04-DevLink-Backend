package com.DevLink.backend.service;

import com.DevLink.backend.dto.NotificationResponse;
import com.DevLink.backend.entity.Notification;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.entity.enums.NotificationType;
import com.DevLink.backend.exception.NotFoundException;
import com.DevLink.backend.exception.UnauthorizedException;
import com.DevLink.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MapperService mapperService;

    @Transactional
    public void create(User user, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapperService::toNotificationResponse)
                .toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to mark this notification as read");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
