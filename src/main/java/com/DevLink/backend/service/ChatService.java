package com.DevLink.backend.service;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.entity.ChatMessage;
import com.DevLink.backend.entity.Conversation;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.exception.BadRequestException;
import com.DevLink.backend.exception.NotFoundException;
import com.DevLink.backend.repository.ChatMessageRepository;
import com.DevLink.backend.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;

    public record SendResult(ChatMessageResponse message, List<String> recipientEmails) {}

    @Transactional
    public ChatConversationResponse getOrCreateConversation(String currentEmail, Long recipientId) {
        User me = userService.getCurrentUserEntity(currentEmail);
        if (me.getId().equals(recipientId)) {
            throw new BadRequestException("No puedes iniciar un chat contigo mismo");
        }
        User other = userService.getPublicUserEntity(recipientId);

        User low  = me.getId() < other.getId() ? me : other;
        User high = me.getId() < other.getId() ? other : me;

        Conversation conv = conversationRepository
                .findByUserLow_IdAndUserHigh_Id(low.getId(), high.getId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder().userLow(low).userHigh(high).build()));

        return toConversationResponse(conv, me.getId());
    }

    @Transactional(readOnly = true)
    public List<ChatConversationResponse> listConversations(String currentEmail) {
        User me = userService.getCurrentUserEntity(currentEmail);
        return conversationRepository.findAllForUser(me.getId()).stream()
                .map(c -> toConversationResponse(c, me.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(String currentEmail, Long conversationId, int page, int size) {
        User me = userService.getCurrentUserEntity(currentEmail);
        Conversation conv = requireMembership(conversationId, me);
        Page<ChatMessage> messages = chatMessageRepository
                .findByConversation_IdOrderByCreatedAtDesc(conv.getId(), PageRequest.of(page, size));
        return messages.getContent().stream().map(this::toMessageResponse).toList();
    }

    @Transactional
    public SendResult saveMessage(String senderEmail, SendChatMessageRequest req) {
        User sender = userService.getCurrentUserEntity(senderEmail);
        Conversation conv = requireMembership(req.conversationId(), sender);

        ChatMessage msg = chatMessageRepository.save(ChatMessage.builder()
                .conversation(conv)
                .sender(sender)
                .content(req.content())
                .type(req.type())
                .language(req.language())
                .build());

        conv.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conv);

        List<String> recipients = List.of(
                conv.getUserLow().getEmail(),
                conv.getUserHigh().getEmail());

        return new SendResult(toMessageResponse(msg), recipients);
    }

    // ---------- helpers ----------

    private Conversation requireMembership(Long conversationId, User user) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversación no encontrada"));
        boolean member = conv.getUserLow().getId().equals(user.getId())
                || conv.getUserHigh().getId().equals(user.getId());
        if (!member) throw new BadRequestException("No perteneces a esta conversación");
        return conv;
    }

    private ChatConversationResponse toConversationResponse(Conversation c, Long myId) {
        User other = c.getUserLow().getId().equals(myId) ? c.getUserHigh() : c.getUserLow();
        String preview = chatMessageRepository
                .findTopByConversation_IdOrderByCreatedAtDesc(c.getId())
                .map(ChatMessage::getContent)
                .orElse(null);
        return new ChatConversationResponse(
                c.getId(),
                new ChatUserResponse(other.getId(), other.getFullName(), other.getGithubUrl()),
                preview,
                c.getLastMessageAt());
    }

    private ChatMessageResponse toMessageResponse(ChatMessage m) {
        return new ChatMessageResponse(
                m.getId(),
                m.getConversation().getId(),
                m.getSender().getId(),
                m.getSender().getFullName(),
                m.getContent(),
                m.getType(),
                m.getLanguage(),
                m.getCreatedAt());
    }
}