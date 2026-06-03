package com.DevLink.backend.controller;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ChatService chatService;

    @GetMapping
    public List<ChatConversationResponse> myConversations(Authentication authentication) {
        return chatService.listConversations(authentication.getName());
    }

    @PostMapping
    public ResponseEntity<ChatConversationResponse> start(
            @Valid @RequestBody StartConversationRequest req, Authentication authentication) {
        return ResponseEntity.ok(
                chatService.getOrCreateConversation(authentication.getName(), req.recipientId()));
    }

    @GetMapping("/{id}/messages")
    public List<ChatMessageResponse> messages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            Authentication authentication) {
        return chatService.getMessages(authentication.getName(), id, page, size);
    }
}