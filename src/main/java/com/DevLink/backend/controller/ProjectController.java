package com.DevLink.backend.controller;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    // ── Projects ──────────────────────────────────────────────────────────────

    @PostMapping("/api/projects")
    public ResponseEntity<ProjectResponse> createDraft(Authentication authentication,
                                                       @Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createDraft(authentication.getName(), request));
    }

    @PutMapping("/api/projects/{id}")
    public ResponseEntity<ProjectResponse> updateDraft(@PathVariable Long id,
                                                       Authentication authentication,
                                                       @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateDraft(id, authentication.getName(), request));
    }

    @PutMapping("/api/projects/{id}/publish")
    public ResponseEntity<ProjectResponse> publish(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(projectService.publish(id, authentication.getName()));
    }

    @PostMapping("/api/projects/{id}/start-development")
    public ResponseEntity<ProjectResponse> startDevelopment(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(projectService.startDevelopment(id, authentication.getName()));
    }

    @PostMapping("/api/projects/{id}/complete")
    public ResponseEntity<ProjectResponse> complete(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(projectService.complete(id, authentication.getName()));
    }

    @GetMapping("/api/projects")
    public ResponseEntity<Map<String, Object>> listPublished(
            @RequestParam(required = false) List<String> technologyIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(projectService.listPublishedProjects(technologyIds, email, page, size));
    }

    @GetMapping("/api/projects/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(projectService.getProject(id, email));
    }

    // ── Applications ──────────────────────────────────────────────────────────

    @PostMapping("/api/projects/{id}/apply")
    public ResponseEntity<ApplicationResponse> apply(@PathVariable Long id,
                                                     Authentication authentication,
                                                     @RequestBody(required = false) Map<String, String> body) {
        String message = body != null ? body.get("message") : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.applyToProject(id, authentication.getName(), message));
    }

    @GetMapping("/api/projects/{id}/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplications(@PathVariable Long id,
                                                                     Authentication authentication) {
        return ResponseEntity.ok(projectService.getApplications(id, authentication.getName()));
    }

    @PutMapping("/api/projects/{projectId}/applications/{applicationId}/accepted")
    public ResponseEntity<ApplicationResponse> acceptApplication(@PathVariable Long projectId,
                                                                  @PathVariable Long applicationId,
                                                                  Authentication authentication) {
        return ResponseEntity.ok(projectService.reviewApplication(projectId, applicationId, authentication.getName(), true));
    }

    @PutMapping("/api/projects/{projectId}/applications/{applicationId}/rejected")
    public ResponseEntity<ApplicationResponse> rejectApplication(@PathVariable Long projectId,
                                                                   @PathVariable Long applicationId,
                                                                   Authentication authentication) {
        return ResponseEntity.ok(projectService.reviewApplication(projectId, applicationId, authentication.getName(), false));
    }

    @GetMapping("/api/applications/me")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication authentication) {
        return ResponseEntity.ok(projectService.getMyApplications(authentication.getName()));
    }
}
