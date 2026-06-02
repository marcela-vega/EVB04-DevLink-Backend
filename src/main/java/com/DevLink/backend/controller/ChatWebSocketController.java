package com.DevLink.backend.controller;

import com.DevLink.backend.dto.SendMessageRequest;
import com.DevLink.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /** Cliente envía a /app/chat.send */
    @MessageMapping("/chat.send")
    public void send(@Payload SendMessageRequest req, Principal principal) {
        ChatService.SendResult result = chatService.saveMessage(principal.getName(), req);

        // Entrega a ambos participantes en su cola privada /user/queue/messages
        for (String email : result.recipientEmails()) {
            messagingTemplate.convertAndSendToUser(email, "/queue/messages", result.message());
        }
    }
}