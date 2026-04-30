package com.DevLink.backend.service;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.entity.Application;
import com.DevLink.backend.entity.Project;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.entity.enums.ApplicationStatus;
import com.DevLink.backend.entity.enums.NotificationType;
import com.DevLink.backend.entity.enums.ProjectStatus;
import com.DevLink.backend.exception.BadRequestException;
import com.DevLink.backend.exception.NotFoundException;
import com.DevLink.backend.exception.UnauthorizedException;
import com.DevLink.backend.repository.ApplicationRepository;
import com.DevLink.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final UserService userService;
    private final TechnologyService technologyService;
    private final MapperService mapperService;
    private final NotificationService notificationService;

    @Transactional
    public ProjectResponse createDraft(String email, CreateProjectRequest request) {
        User creator = userService.getCurrentUserEntity(email);

        List<Integer> techIds = request.stackRequired() != null
                ? request.stackRequired().stream().map(Integer::valueOf).toList()
                : List.of();

        ProjectStatus initialStatus = "seeking_collaborators".equals(request.status())
                ? ProjectStatus.LOOKING_FOR_COLLABORATORS
                : ProjectStatus.DRAFT;

        Project project = Project.builder()
                .creator(creator)
                .title(request.title().trim())
                .description(request.description().trim())
                .status(initialStatus)
                .technologies(new HashSet<>(technologyService.getTechnologiesByIds(techIds)))
                .build();

        Project saved = projectRepository.save(project);
        return mapperService.toProjectResponse(saved, creator.getId(), false);
    }

    @Transactional
    public ProjectResponse updateDraft(Long projectId, String email, UpdateProjectRequest request) {
        User currentUser = userService.getCurrentUserEntity(email);
        Project project = getProjectEntity(projectId);
        validateOwner(project, currentUser.getId());

        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new BadRequestException("Only draft projects can be edited");
        }

        if (request.title() != null) project.setTitle(request.title().trim());
        if (request.description() != null) project.setDescription(request.description().trim());
        if (request.stackRequired() != null) {
            List<Integer> techIds = request.stackRequired().stream().map(Integer::valueOf).toList();
            project.setTechnologies(new HashSet<>(technologyService.getTechnologiesByIds(techIds)));
        }

        Project saved = projectRepository.save(project);
        return mapperService.toProjectResponse(saved, currentUser.getId(), false);
    }

    @Transactional
    public ProjectResponse publish(Long projectId, String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        Project project = getProjectEntity(projectId);
        validateOwner(project, currentUser.getId());

        if (project.getTechnologies().isEmpty() || project.getTitle().isBlank() || project.getDescription().isBlank()) {
            throw new BadRequestException("Project must have all mandatory information before publishing");
        }

        project.setStatus(ProjectStatus.LOOKING_FOR_COLLABORATORS);
        Project saved = projectRepository.save(project);

        notificationService.create(currentUser,
                "Project published",
                "Your project '" + saved.getTitle() + "' is now visible in the public feed.",
                NotificationType.PROJECT_PUBLISHED);

        return mapperService.toProjectResponse(saved, currentUser.getId(), false);
    }

    @Transactional
    public ProjectResponse startDevelopment(Long projectId, String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        Project project = getProjectEntity(projectId);
        validateOwner(project, currentUser.getId());

        if (project.getStatus() != ProjectStatus.LOOKING_FOR_COLLABORATORS) {
            throw new BadRequestException("Project must be in seeking_collaborators state to start development");
        }

        project.setStatus(ProjectStatus.IN_DEVELOPMENT);
        project.setStartedAt(LocalDateTime.now());
        Project saved = projectRepository.save(project);

        notificationService.create(currentUser,
                "Project started",
                "Your project '" + saved.getTitle() + "' is now in development.",
                NotificationType.PROJECT_STARTED);

        return mapperService.toProjectResponse(saved, currentUser.getId(), false);
    }

    @Transactional
    public ProjectResponse complete(Long projectId, String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        Project project = getProjectEntity(projectId);
        validateOwner(project, currentUser.getId());

        if (project.getStatus() != ProjectStatus.IN_DEVELOPMENT) {
            throw new BadRequestException("Project must be in in_development state to complete");
        }

        project.setStatus(ProjectStatus.COMPLETED);
        project.setCompletedAt(LocalDateTime.now());
        Project saved = projectRepository.save(project);

        return mapperService.toProjectResponse(saved, currentUser.getId(), false);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listPublishedProjects(List<String> technologyIdStrings, String emailOrNull, int page, int size) {
        Long currentUserId = null;
        if (emailOrNull != null) {
            currentUserId = userService.getCurrentUserEntity(emailOrNull).getId();
        }

        List<Project> projects;
        if (technologyIdStrings == null || technologyIdStrings.isEmpty()) {
            projects = projectRepository.findByStatusOrderByCreatedAtDesc(ProjectStatus.LOOKING_FOR_COLLABORATORS);
        } else {
            List<Integer> techIds = technologyIdStrings.stream().map(Integer::valueOf).distinct().toList();
            List<Long> projectIds = projectRepository.findPublishedIdsByAllTechnologies(
                    ProjectStatus.LOOKING_FOR_COLLABORATORS.name(), techIds, techIds.size());
            projects = projectIds.stream().map(this::getProjectEntity).toList();
        }

        Long finalCurrentUserId = currentUserId;
        List<ProjectResponse> allContent = projects.stream()
                .map(project -> mapperService.toProjectResponse(
                        project,
                        finalCurrentUserId,
                        finalCurrentUserId != null && applicationRepository.existsByProjectIdAndApplicantId(project.getId(), finalCurrentUserId)
                ))
                .toList();

        int total = allContent.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<ProjectResponse> content = allContent.subList(fromIndex, toIndex);
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 1;

        return Map.of(
                "content", content,
                "totalElements", total,
                "number", page,
                "size", size,
                "totalPages", totalPages
        );
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId, String emailOrNull) {
        Project project = getProjectEntity(projectId);
        Long currentUserId = null;
        if (emailOrNull != null) {
            currentUserId = userService.getCurrentUserEntity(emailOrNull).getId();
        }

        boolean isOwner = currentUserId != null && project.getCreator().getId().equals(currentUserId);
        if (project.getStatus() == ProjectStatus.DRAFT && !isOwner) {
            throw new UnauthorizedException("Draft projects are private and visible only to the creator");
        }

        boolean hasApplied = currentUserId != null && applicationRepository.existsByProjectIdAndApplicantId(projectId, currentUserId);
        return mapperService.toProjectResponse(project, currentUserId, hasApplied);
    }

    @Transactional
    public ApplicationResponse applyToProject(Long projectId, String email, String message) {
        User applicant = userService.getCurrentUserEntity(email);
        Project project = getProjectEntity(projectId);

        if (project.getStatus() != ProjectStatus.LOOKING_FOR_COLLABORATORS) {
            throw new BadRequestException("Only published projects can receive applications");
        }
        if (project.getCreator().getId().equals(applicant.getId())) {
            throw new BadRequestException("You cannot apply to your own project");
        }
        if (applicationRepository.existsByProjectIdAndApplicantId(projectId, applicant.getId())) {
            throw new BadRequestException("You have already applied to this project");
        }
        if (!projectUsesAnyApplicantTechnology(project, applicant)) {
            throw new BadRequestException("You can only apply to projects that match at least one technology in your stack");
        }

        Application application = Application.builder()
                .project(project)
                .applicant(applicant)
                .message(message)
                .build();
        Application saved = applicationRepository.save(application);

        notificationService.create(project.getCreator(),
                "New application received",
                applicant.getFullName() + " applied to your project '" + project.getTitle() + "'.",
                NotificationType.APPLICATION_RECEIVED);

        return mapperService.toApplicationResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplications(Long projectId, String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        Project project = getProjectEntity(projectId);
        validateOwner(project, currentUser.getId());

        return applicationRepository.findByProjectIdOrderByAppliedAtDesc(projectId)
                .stream()
                .map(mapperService::toApplicationResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse reviewApplication(Long projectId, Long applicationId, String email, boolean accepted) {
        User currentUser = userService.getCurrentUserEntity(email);
        Project project = getProjectEntity(projectId);
        validateOwner(project, currentUser.getId());

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (!application.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Application does not belong to this project");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Application has already been reviewed");
        }

        application.setStatus(accepted ? ApplicationStatus.ACCEPTED : ApplicationStatus.REJECTED);
        Application saved = applicationRepository.save(application);

        NotificationType notifType = accepted ? NotificationType.APPLICATION_ACCEPTED : NotificationType.APPLICATION_REJECTED;
        String notifTitle = accepted ? "Application accepted" : "Application rejected";
        String notifMessage = accepted
                ? "Your application to '" + project.getTitle() + "' was accepted!"
                : "Your application to '" + project.getTitle() + "' was not accepted.";

        notificationService.create(application.getApplicant(), notifTitle, notifMessage, notifType);

        return mapperService.toApplicationResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        return applicationRepository.findByApplicantIdOrderByAppliedAtDesc(currentUser.getId())
                .stream()
                .map(mapperService::toApplicationResponse)
                .toList();
    }

    private Project getProjectEntity(Long projectId) {
        return projectRepository.findDetailedById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private void validateOwner(Project project, Long currentUserId) {
        if (!project.getCreator().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to modify this project");
        }
    }

    private boolean projectUsesAnyApplicantTechnology(Project project, User applicant) {
        return applicant.getTechnologies().stream()
                .anyMatch(userTech -> project.getTechnologies().stream()
                        .anyMatch(projectTech -> projectTech.getId().equals(userTech.getId())));
    }
}
