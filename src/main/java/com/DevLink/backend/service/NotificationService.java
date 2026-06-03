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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MapperService mapperService;
    private final SimpMessagingTemplate messagingTemplate;

    // Compatibilidad: las llamadas existentes (4 args) siguen funcionando
    @Transactional
    public void create(User user, String title, String message, NotificationType type) {
        create(user, title, message, type, null);
    }

    @Transactional
    public void create(User user, String title, String message, NotificationType type, String link) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .link(link)
                .build();
        Notification saved = notificationRepository.save(notification);

        // Empuje en tiempo real al destinatario (mismo /ws + Principal = email)
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications",
                mapperService.toNotificationResponse(saved));
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

    @Transactional(readOnly = true)
    public List<NotificationResponse> getForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapperService::toNotificationResponse)
                .toList();
    }
}
