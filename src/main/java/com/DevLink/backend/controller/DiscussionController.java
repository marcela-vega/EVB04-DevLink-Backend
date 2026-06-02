package com.DevLink.backend.controller;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.service.DiscussionService;
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
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {
    private final DiscussionService discussionService;

    @PostMapping
    public ResponseEntity<DiscussionResponse> create(Authentication authentication,
                                                      @Valid @RequestBody CreateDiscussionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(discussionService.create(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<Page<DiscussionResponse>> listAll(@RequestParam(required = false) List<Integer> technologyIds,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(discussionService.listAll(technologyIds, pageable));
    }

    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<Page<DiscussionResponse>> listByProject(@PathVariable Long projectId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(discussionService.listByProject(projectId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscussionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(discussionService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscussionResponse> update(@PathVariable Long id,
                                                      Authentication authentication,
                                                      @Valid @RequestBody UpdateDiscussionRequest request) {
        return ResponseEntity.ok(discussionService.update(id, authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiMessageResponse> delete(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(discussionService.delete(id, authentication.getName()));
    }
}
