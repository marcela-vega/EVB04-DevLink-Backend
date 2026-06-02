package com.DevLink.backend.controller;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public List<ConversationResponse> myConversations(Principal principal) {
        return chatService.listConversations(principal.getName());
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> startConversation(
            @Valid @RequestBody StartConversationRequest req, Principal principal) {
        return ResponseEntity.ok(
                chatService.getOrCreateConversation(principal.getName(), req.recipientId()));
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> messages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            Principal principal) {
        return chatService.getMessages(principal.getName(), id, page, size);
    }
}