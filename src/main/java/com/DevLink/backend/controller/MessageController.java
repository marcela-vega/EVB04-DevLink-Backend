package com.DevLink.backend.controller;

import com.DevLink.backend.dto.ConversationResponse;
import com.DevLink.backend.dto.CreateMessageRequest;
import com.DevLink.backend.dto.MessageResponse;
import com.DevLink.backend.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> send(Authentication authentication,
                                                 @Valid @RequestBody CreateMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.send(authentication.getName(), request));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(Authentication authentication) {
        return ResponseEntity.ok(messageService.getConversations(authentication.getName()));
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<Page<MessageResponse>> getConversation(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(messageService.getConversation(authentication.getName(), userId, pageable));
    }
}
