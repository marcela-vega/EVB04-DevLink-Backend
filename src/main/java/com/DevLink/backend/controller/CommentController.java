package com.DevLink.backend.controller;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/api/discussions/{discussionId}/comments")
    public ResponseEntity<CommentResponse> create(@PathVariable Long discussionId,
                                                   Authentication authentication,
                                                   @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(discussionId, authentication.getName(), request));
    }

    @GetMapping("/api/discussions/{discussionId}/comments")
    public ResponseEntity<List<CommentResponse>> getByDiscussion(@PathVariable Long discussionId) {
        return ResponseEntity.ok(commentService.getByDiscussion(discussionId));
    }

    @PutMapping("/api/comments/{id}")
    public ResponseEntity<CommentResponse> update(@PathVariable Long id,
                                                   Authentication authentication,
                                                   @Valid @RequestBody UpdateCommentRequest request) {
        return ResponseEntity.ok(commentService.update(id, authentication.getName(), request));
    }

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<ApiMessageResponse> delete(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(commentService.delete(id, authentication.getName()));
    }
}
