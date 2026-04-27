package com.DevLink.backend.controller;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createDraft(Authentication authentication,
                                                       @Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createDraft(authentication.getName(), request));
    }

    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<ProjectResponse> updateDraft(@PathVariable Long id,
                                                       Authentication authentication,
                                                       @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateDraft(id, authentication.getName(), request));
    }

    @PutMapping("/{id:[0-9]+}/publish")
    public ResponseEntity<ProjectResponse> publish(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(projectService.publish(id, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> listPublished(@RequestParam(required = false) List<Integer> technologyIds,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size,
                                                               Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(projectService.listPublishedProjects(technologyIds, email, pageable));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ProjectResponse>> listMyProjects(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size,
                                                                 Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(projectService.listMyProjects(authentication.getName(), pageable));
    }

    @GetMapping("/my/drafts")
    public ResponseEntity<Page<ProjectResponse>> listMyDrafts(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size,
                                                               Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(projectService.listMyDrafts(authentication.getName(), pageable));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(projectService.getProject(id, email));
    }

    @PostMapping("/{id:[0-9]+}/apply")
    public ResponseEntity<ApiMessageResponse> apply(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.applyToProject(id, authentication.getName()));
    }

    @GetMapping("/{id:[0-9]+}/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplications(@PathVariable Long id,
                                                                     Authentication authentication) {
        return ResponseEntity.ok(projectService.getApplications(id, authentication.getName()));
    }

    @PutMapping("/{projectId:[0-9]+}/applications/{applicationId:[0-9]+}/accept")
    public ResponseEntity<ApplicationResponse> acceptApplication(@PathVariable Long projectId,
                                                                  @PathVariable Long applicationId,
                                                                  Authentication authentication) {
        return ResponseEntity.ok(projectService.acceptApplication(projectId, applicationId, authentication.getName()));
    }

    @PutMapping("/{projectId:[0-9]+}/applications/{applicationId:[0-9]+}/reject")
    public ResponseEntity<ApplicationResponse> rejectApplication(@PathVariable Long projectId,
                                                                  @PathVariable Long applicationId,
                                                                  Authentication authentication) {
        return ResponseEntity.ok(projectService.rejectApplication(projectId, applicationId, authentication.getName()));
    }

    @PutMapping("/{projectId:[0-9]+}/applications/{applicationId:[0-9]+}/withdraw")
    public ResponseEntity<ApplicationResponse> withdrawApplication(@PathVariable Long projectId,
                                                                    @PathVariable Long applicationId,
                                                                    Authentication authentication) {
        return ResponseEntity.ok(projectService.withdrawApplication(projectId, applicationId, authentication.getName()));
    }
}
