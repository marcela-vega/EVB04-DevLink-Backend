package com.DevLink.backend.controller;

import com.DevLink.backend.dto.SendChatMessageRequest;
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

    @MessageMapping("/chat.send") // cliente publica en /app/chat.send
    public void send(@Payload SendChatMessageRequest req, Principal principal) {
        ChatService.SendResult result = chatService.saveMessage(principal.getName(), req);
        for (String email : result.recipientEmails()) {
            messagingTemplate.convertAndSendToUser(email, "/queue/messages", result.message());
        }
    }
}