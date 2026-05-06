package com.DevLink.backend.service;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.entity.*;
import com.DevLink.backend.entity.enums.ApplicationStatus;
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
                .sorted()
                .findFirst()
                .orElse("DEVELOPER")
                .toLowerCase();

        List<String> stack = user.getTechnologies().stream()
                .sorted(Comparator.comparing(Technology::getName))
                .map(Technology::getName)
                .toList();

        long projectsCount = projectRepository.countByCreatorId(user.getId());
        long collaborationsCount = applicationRepository.countByApplicantIdAndStatus(
                user.getId(), ApplicationStatus.ACCEPTED);

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                role,
                Boolean.TRUE.equals(user.getActive()) ? "active" : "suspended",
                stack,
                user.getBio(),
                null,
                user.getGithubUrl(),
                user.getGitlabUrl(),
                projectsCount,
                collaborationsCount,
                user.getCreatedAt()
        );
    }

    public ProjectResponse toProjectResponse(Project project, Long currentUserId, boolean hasAlreadyApplied) {
        boolean canApply = currentUserId != null
                && project.getCreator() != null
                && !project.getCreator().getId().equals(currentUserId)
                && project.getStatus() == ProjectStatus.LOOKING_FOR_COLLABORATORS
                && !hasAlreadyApplied;

        List<String> stackRequired = project.getTechnologies().stream()
                .sorted(Comparator.comparing(Technology::getName))
                .map(Technology::getName)
                .toList();

        User creator = project.getCreator();
        ProjectResponse.CreatorInfo creatorInfo = new ProjectResponse.CreatorInfo(
                creator.getId(), creator.getFullName(), null);

        long applicationCount = applicationRepository.countByProjectId(project.getId());

        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                stackRequired,
                toProjectStatus(project.getStatus()),
                creator.getId(),
                creatorInfo,
                List.of(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                null,
                null,
                applicationCount,
                canApply
        );
    }

    public ApplicationResponse toApplicationResponse(Application application) {
        User applicant = application.getApplicant();
        List<String> stack = applicant.getTechnologies().stream()
                .sorted(Comparator.comparing(Technology::getName))
                .map(Technology::getName)
                .toList();
        ApplicationResponse.ApplicantInfo applicantInfo = new ApplicationResponse.ApplicantInfo(
                applicant.getId(),
                applicant.getFullName(),
                applicant.getEmail(),
                stack,
                null,
                applicant.getBio(),
                applicant.getGithubUrl(),
                applicant.getGitlabUrl()
        );
        return new ApplicationResponse(
                application.getId(),
                application.getProject().getId(),
                applicant.getId(),
                applicantInfo,
                null,
                toApplicationStatus(application.getStatus()),
                application.getAppliedAt(),
                application.getAppliedAt()
        );
    }

    private String toProjectStatus(ProjectStatus status) {
        return switch (status) {
            case DRAFT -> "draft";
            case LOOKING_FOR_COLLABORATORS -> "seeking_collaborators";
            case IN_DEVELOPMENT -> "in_development";
            case COMPLETED -> "completed";
        };
    }

    private String toApplicationStatus(ApplicationStatus status) {
        return switch (status) {
            case PENDING -> "pending";
            case ACCEPTED -> "accepted";
            case REJECTED -> "rejected";
            case WITHDRAWN -> "closed";
        };
    }

    public NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name().toLowerCase(),
                notification.getTitle(),
                notification.getMessage(),
                Boolean.TRUE.equals(notification.getIsRead()),
                null,
                notification.getCreatedAt()
        );
    }

    public List<TechnologyResponse> toTechnologyResponses(List<Technology> technologies) {
        return technologies.stream().map(this::toTechnologyResponse).toList();
    }

    public DiscussionResponse toDiscussionResponse(Discussion discussion, int commentCount) {
        return new DiscussionResponse(
                discussion.getId(),
                discussion.getTitle(),
                discussion.getContent(),
                discussion.getAuthor().getId(),
                discussion.getAuthor().getFullName(),
                discussion.getTechnologies().stream()
                        .sorted(Comparator.comparing(Technology::getName))
                        .map(this::toTechnologyResponse)
                        .toList(),
                commentCount,
                discussion.getCreatedAt(),
                discussion.getUpdatedAt()
        );
    }

    public CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getAuthor().getFullName(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getFullName(),
                message.getReceiver().getId(),
                message.getReceiver().getFullName(),
                message.getContent(),
                Boolean.TRUE.equals(message.getIsRead()),
                message.getCreatedAt()
        );
    }
}
