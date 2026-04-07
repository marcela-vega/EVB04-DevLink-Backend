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

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateDraft(@PathVariable Long id,
                                                       Authentication authentication,
                                                       @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateDraft(id, authentication.getName(), request));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ProjectResponse> publish(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(projectService.publish(id, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listPublished(@RequestParam(required = false) List<Integer> technologyIds,
                                                               Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(projectService.listPublishedProjects(technologyIds, email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(projectService.getProject(id, email));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<ApiMessageResponse> apply(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.applyToProject(id, authentication.getName()));
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplications(@PathVariable Long id,
                                                                     Authentication authentication) {
        return ResponseEntity.ok(projectService.getApplications(id, authentication.getName()));
    }
}
