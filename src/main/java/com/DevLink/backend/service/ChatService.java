package com.DevLink.backend.service;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.entity.Conversation;
import com.DevLink.backend.entity.Message;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.repository.ConversationRepository;
import com.DevLink.backend.repository.MessageRepository;
import com.DevLink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /** Resultado del envío: el mensaje + a quién hay que entregárselo en vivo. */
    public record SendResult(MessageResponse message, List<String> recipientEmails) {}

    @Transactional
    public ConversationResponse getOrCreateConversation(String currentEmail, Long recipientId) {
        User me = requireUser(currentEmail);
        if (me.getId().equals(recipientId)) {
            throw new IllegalArgumentException("No puedes iniciar un chat contigo mismo");
        }
        User other = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario destino no existe"));

        User low  = me.getId() < other.getId() ? me : other;
        User high = me.getId() < other.getId() ? other : me;

        Conversation conv = conversationRepository
                .findByUserLow_IdAndUserHigh_Id(low.getId(), high.getId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder().userLow(low).userHigh(high).build()));

        return toConversationResponse(conv, me.getId());
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> listConversations(String currentEmail) {
        User me = requireUser(currentEmail);
        return conversationRepository.findAllForUser(me.getId()).stream()
                .map(c -> toConversationResponse(c, me.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(String currentEmail, Long conversationId, int page, int size) {
        User me = requireUser(currentEmail);
        Conversation conv = requireMembership(conversationId, me);
        Page<Message> messages = messageRepository.findByConversation_IdOrderByCreatedAtDesc(
                conv.getId(), PageRequest.of(page, size));
        // Se devuelven de más reciente a más antiguo; el front los invierte para mostrar.
        return messages.getContent().stream().map(this::toMessageResponse).toList();
    }

    @Transactional
    public SendResult saveMessage(String senderEmail, SendMessageRequest req) {
        User sender = requireUser(senderEmail);
        Conversation conv = requireMembership(req.conversationId(), sender);

        Message msg = messageRepository.save(Message.builder()
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

    private User requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private Conversation requireMembership(Long conversationId, User user) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversación no existe"));
        boolean member = conv.getUserLow().getId().equals(user.getId())
                || conv.getUserHigh().getId().equals(user.getId());
        if (!member) throw new AccessDeniedException("No perteneces a esta conversación");
        return conv;
    }

    private ConversationResponse toConversationResponse(Conversation c, Long myId) {
        User other = c.getUserLow().getId().equals(myId) ? c.getUserHigh() : c.getUserLow();
        String preview = messageRepository
                .findTopByConversation_IdOrderByCreatedAtDesc(c.getId())
                .map(Message::getContent)
                .orElse(null);
        return new ConversationResponse(
                c.getId(),
                new ChatUserResponse(other.getId(), other.getFullName(), other.getGithubUrl()),
                preview,
                c.getLastMessageAt());
    }

    private MessageResponse toMessageResponse(Message m) {
        return new MessageResponse(
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