package com.DevLink.backend.service;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.entity.*;
import com.DevLink.backend.entity.enums.ProjectStatus;
import com.DevLink.backend.repository.ApplicationRepository;
import com.DevLink.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapperService {

    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;

    public TechnologyResponse toTechnologyResponse(Technology technology) {
        return new TechnologyResponse(technology.getId(), technology.getName());
    }

    public UserProfileResponse toUserProfileResponse(User user) {
        String role = user.getRoles().stream()
                .map(Role::getName)
                .min(Comparator.naturalOrder())
                .map(String::toLowerCase)
                .orElse("developer");

        String status = Boolean.TRUE.equals(user.getActive()) ? "active" : "suspended";

        List<String> stack = user.getTechnologies().stream()
                .sorted(Comparator.comparing(Technology::getName))
                .map(t -> String.valueOf(t.getId()))
                .toList();

        long projectsCount = projectRepository.countByCreatorIdAndStatusNot(
                user.getId(), ProjectStatus.DRAFT);
        int collaborationsCount = applicationRepository.countByApplicantIdAndStatus(
                user.getId(), com.DevLink.backend.entity.enums.ApplicationStatus.ACCEPTED);

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                role,
                status,
                stack,
                user.getBio(),
                null, // avatar not stored in DB
                user.getCreatedAt(),
                projectsCount,
                collaborationsCount,
                user.getGithubUrl(),
                user.getGitlabUrl()
        );
    }

    public ProjectResponse toProjectResponse(Project project, Long currentUserId, boolean hasAlreadyApplied) {
        boolean canApply = currentUserId != null
                && project.getCreator() != null
                && !project.getCreator().getId().equals(currentUserId)
                && "LOOKING_FOR_COLLABORATORS".equals(project.getStatus().name())
                && !hasAlreadyApplied;

        List<String> stackRequired = project.getTechnologies().stream()
                .sorted(Comparator.comparing(Technology::getName))
                .map(t -> String.valueOf(t.getId()))
                .toList();

        UserProfileResponse creator = toUserProfileResponse(project.getCreator());

        // map status to frontend naming convention
        String status = switch (project.getStatus()) {
            case DRAFT -> "draft";
            case LOOKING_FOR_COLLABORATORS -> "seeking_collaborators";
            case IN_DEVELOPMENT -> "in_development";
            case COMPLETED -> "completed";
        };

        int applicationCount = applicationRepository.countByProjectId(project.getId());

        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                stackRequired,
                status,
                project.getCreator().getId(),
                creator,
                List.of(), // collaborators loaded separately when needed
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getStartedAt(),
                project.getCompletedAt(),
                applicationCount,
                canApply
        );
    }

    public ApplicationResponse toApplicationResponse(Application application) {
        UserProfileResponse applicant = toUserProfileResponse(application.getApplicant());
        ProjectResponse project = toProjectResponse(application.getProject(), application.getApplicant().getId(), true);

        String status = application.getStatus().name().toLowerCase();

        return new ApplicationResponse(
                application.getId(),
                application.getProject().getId(),
                project,
                application.getApplicant().getId(),
                applicant,
                application.getMessage(),
                status,
                application.getAppliedAt(),
                application.getUpdatedAt()
        );
    }

    public NotificationResponse toNotificationResponse(Notification notification) {
        String type = notification.getType().name().toLowerCase();
        return new NotificationResponse(
                notification.getId(),
                type,
                notification.getTitle(),
                notification.getMessage(),
                Boolean.TRUE.equals(notification.getIsRead()),
                null, // link field not stored in DB
                notification.getCreatedAt()
        );
    }

    public List<TechnologyResponse> toTechnologyResponses(List<Technology> technologies) {
        return technologies.stream().map(this::toTechnologyResponse).toList();
    }
}
