package com.DevLink.backend.service;

import com.DevLink.backend.dto.ConversationResponse;
import com.DevLink.backend.dto.CreateMessageRequest;
import com.DevLink.backend.dto.MessageResponse;
import com.DevLink.backend.entity.Message;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.entity.enums.NotificationType;
import com.DevLink.backend.exception.BadRequestException;
import com.DevLink.backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final MapperService mapperService;
    private final NotificationService notificationService;

    @Transactional
    public MessageResponse send(String email, CreateMessageRequest request) {
        User sender = userService.getCurrentUserEntity(email);
        if (sender.getId().equals(request.receiverId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }
        User receiver = userService.getPublicUserEntity(request.receiverId());
        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.content().trim())
                .build();
        Message saved = messageRepository.save(message);
        notificationService.create(receiver,
                "New message",
                sender.getFullName() + " sent you a message.",
                NotificationType.NEW_MESSAGE);
        return mapperService.toMessageResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        List<Message> latestMessages = messageRepository.findLatestMessagePerConversation(currentUser.getId());
        return latestMessages.stream()
                .map(m -> {
                    Long partnerId = m.getSender().getId().equals(currentUser.getId())
                            ? m.getReceiver().getId() : m.getSender().getId();
                    String partnerName = m.getSender().getId().equals(currentUser.getId())
                            ? m.getReceiver().getFullName() : m.getSender().getFullName();
                    long unread = messageRepository.countByReceiverIdAndSenderIdAndIsReadFalse(
                            currentUser.getId(), partnerId);
                    return new ConversationResponse(partnerId, partnerName, m.getContent(), m.getCreatedAt(), unread);
                })
                .toList();
    }

    @Transactional
    public Page<MessageResponse> getConversation(String email, Long otherUserId, Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity(email);
        messageRepository.markConversationAsRead(currentUser.getId(), otherUserId);
        return messageRepository.findConversation(currentUser.getId(), otherUserId, pageable)
                .map(mapperService::toMessageResponse);
    }
}
