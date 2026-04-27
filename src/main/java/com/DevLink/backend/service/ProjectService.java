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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

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

        Project project = Project.builder()
                .creator(creator)
                .title(request.title().trim())
                .description(request.description().trim())
                .status(ProjectStatus.DRAFT)
                .technologies(new HashSet<>(technologyService.getTechnologiesByIds(request.technologyIds())))
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
            throw new BadRequestException("Only draft projects can be edited in Hito 1");
        }

        project.setTitle(request.title().trim());
        project.setDescription(request.description().trim());
        project.setTechnologies(new HashSet<>(technologyService.getTechnologiesByIds(request.technologyIds())));

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

    @Transactional(readOnly = true)
    public Page<ProjectResponse> listPublishedProjects(List<Integer> technologyIds, String emailOrNull, Pageable pageable) {
        Long currentUserId = null;
        if (emailOrNull != null) {
            currentUserId = userService.getCurrentUserEntity(emailOrNull).getId();
        }

        Page<Project> projectsPage;
        if (technologyIds == null || technologyIds.isEmpty()) {
            projectsPage = projectRepository.findByStatusOrderByCreatedAtDesc(ProjectStatus.LOOKING_FOR_COLLABORATORS, pageable);
        } else {
            List<Integer> uniqueTechnologyIds = new HashSet<>(technologyIds).stream().toList();
            Page<Long> projectIdsPage = projectRepository.findPublishedIdsByAllTechnologies(
                    ProjectStatus.LOOKING_FOR_COLLABORATORS.name(),
                    uniqueTechnologyIds,
                    uniqueTechnologyIds.size(),
                    pageable
            );
            projectsPage = projectIdsPage.map(this::getProjectEntity);
        }

        Long finalCurrentUserId = currentUserId;
        return projectsPage.map(project -> mapperService.toProjectResponse(
                project,
                finalCurrentUserId,
                finalCurrentUserId != null && applicationRepository.existsByProjectIdAndApplicantId(project.getId(), finalCurrentUserId)
        ));
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> listMyProjects(String email, Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity(email);
        Page<Project> projectsPage = projectRepository.findByCreatorIdAndStatusOrderByCreatedAtDesc(
                currentUser.getId(), ProjectStatus.LOOKING_FOR_COLLABORATORS, pageable);
        return projectsPage.map(project -> mapperService.toProjectResponse(
                project,
                currentUser.getId(),
                applicationRepository.existsByProjectIdAndApplicantId(project.getId(), currentUser.getId())
        ));
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> listMyDrafts(String email, Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity(email);
        Page<Project> projectsPage = projectRepository.findByCreatorIdAndStatusOrderByCreatedAtDesc(
                currentUser.getId(), ProjectStatus.DRAFT, pageable);
        return projectsPage.map(project -> mapperService.toProjectResponse(
                project,
                currentUser.getId(),
                false
        ));
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
    public ApiMessageResponse applyToProject(Long projectId, String email) {
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
                .build();
        applicationRepository.save(application);

        notificationService.create(project.getCreator(),
                "New application received",
                applicant.getFullName() + " applied to your project '" + project.getTitle() + "'.",
                NotificationType.APPLICATION_RECEIVED);

        return new ApiMessageResponse("Application sent successfully");
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
    public ApplicationResponse acceptApplication(Long projectId, Long applicationId, String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        Project project = getProjectEntity(projectId);
        validateOwner(project, currentUser.getId());

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (!application.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Application does not belong to this project");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Only pending applications can be accepted");
        }

        application.setStatus(ApplicationStatus.ACCEPTED);
        Application saved = applicationRepository.save(application);

        notificationService.create(application.getApplicant(),
                "Application accepted",
                "Your application to project '" + project.getTitle() + "' has been accepted!",
                NotificationType.APPLICATION_ACCEPTED);

        return mapperService.toApplicationResponse(saved);
    }

    @Transactional
    public ApplicationResponse rejectApplication(Long projectId, Long applicationId, String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        Project project = getProjectEntity(projectId);
        validateOwner(project, currentUser.getId());

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (!application.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Application does not belong to this project");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Only pending applications can be rejected");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        Application saved = applicationRepository.save(application);

        notificationService.create(application.getApplicant(),
                "Application rejected",
                "Your application to project '" + project.getTitle() + "' has been rejected.",
                NotificationType.APPLICATION_REJECTED);

        return mapperService.toApplicationResponse(saved);
    }

    @Transactional
    public ApplicationResponse withdrawApplication(Long projectId, Long applicationId, String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        Project project = getProjectEntity(projectId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (!application.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Application does not belong to this project");
        }

        if (!application.getApplicant().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only withdraw your own applications");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Only pending applications can be withdrawn");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        Application saved = applicationRepository.save(application);

        notificationService.create(project.getCreator(),
                "Application withdrawn",
                currentUser.getFullName() + " has withdrawn their application to your project '" + project.getTitle() + "'.",
                NotificationType.APPLICATION_WITHDRAWN);

        return mapperService.toApplicationResponse(saved);
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
