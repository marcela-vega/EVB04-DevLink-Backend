package com.DevLink.backend.service;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.entity.*;
import com.DevLink.backend.entity.enums.ApplicationStatus;
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
                && "LOOKING_FOR_COLLABORATORS".equals(project.getStatus().name())
                && !hasAlreadyApplied;

        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStatus().name(),
                project.getCreator().getId(),
                project.getCreator().getFullName(),
                project.getTechnologies().stream()
                        .sorted(Comparator.comparing(Technology::getName))
                        .map(this::toTechnologyResponse)
                        .toList(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                canApply
        );
    }

    public ApplicationResponse toApplicationResponse(Application application) {
        User applicant = application.getApplicant();
        return new ApplicationResponse(
                application.getId(),
                application.getStatus().name(),
                application.getAppliedAt(),
                applicant.getId(),
                applicant.getFullName(),
                applicant.getEmail(),
                applicant.getBio(),
                applicant.getGithubUrl(),
                applicant.getGitlabUrl(),
                applicant.getTechnologies().stream()
                        .sorted(Comparator.comparing(Technology::getName))
                        .map(this::toTechnologyResponse)
                        .toList()
        );
    }

    public NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType().name(),
                Boolean.TRUE.equals(notification.getIsRead()),
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
