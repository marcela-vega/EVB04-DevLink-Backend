package com.DevLink.backend.service;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.entity.Discussion;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.entity.enums.NotificationType;
import com.DevLink.backend.exception.NotFoundException;
import com.DevLink.backend.exception.UnauthorizedException;
import com.DevLink.backend.repository.CommentRepository;
import com.DevLink.backend.repository.DiscussionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscussionService {
    private final DiscussionRepository discussionRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final TechnologyService technologyService;
    private final MapperService mapperService;
    private final NotificationService notificationService;

    @Transactional
    public DiscussionResponse create(String email, CreateDiscussionRequest request) {
        User author = userService.getCurrentUserEntity(email);
        Discussion discussion = Discussion.builder()
                .author(author)
                .title(request.title().trim())
                .content(request.content().trim())
                .technologies(new HashSet<>(technologyService.getTechnologiesByIds(request.technologyIds())))
                .build();
        Discussion saved = discussionRepository.save(discussion);
        notificationService.create(author, "Discussion created",
                "Your discussion '" + saved.getTitle() + "' has been published.",
                NotificationType.NEW_DISCUSSION);
        return mapperService.toDiscussionResponse(saved, 0);
    }

    @Transactional
    public DiscussionResponse update(Long discussionId, String email, UpdateDiscussionRequest request) {
        User currentUser = userService.getCurrentUserEntity(email);
        Discussion discussion = getDiscussionEntity(discussionId);
        validateAuthor(discussion, currentUser.getId());
        discussion.setTitle(request.title().trim());
        discussion.setContent(request.content().trim());
        discussion.setTechnologies(new HashSet<>(technologyService.getTechnologiesByIds(request.technologyIds())));
        Discussion saved = discussionRepository.save(discussion);
        int commentCount = (int) commentRepository.countByDiscussionId(discussionId);
        return mapperService.toDiscussionResponse(saved, commentCount);
    }

    @Transactional
    public ApiMessageResponse delete(Long discussionId, String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        Discussion discussion = getDiscussionEntity(discussionId);
        validateAuthor(discussion, currentUser.getId());
        discussionRepository.delete(discussion);
        return new ApiMessageResponse("Discussion deleted successfully");
    }

    @Transactional(readOnly = true)
    public DiscussionResponse getById(Long discussionId) {
        Discussion discussion = getDiscussionEntity(discussionId);
        int commentCount = (int) commentRepository.countByDiscussionId(discussionId);
        return mapperService.toDiscussionResponse(discussion, commentCount);
    }

    @Transactional(readOnly = true)
    public Page<DiscussionResponse> listAll(List<Integer> technologyIds, Pageable pageable) {
        Page<Discussion> discussionsPage;
        if (technologyIds == null || technologyIds.isEmpty()) {
            discussionsPage = discussionRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            List<Integer> uniqueIds = new HashSet<>(technologyIds).stream().toList();
            Page<Long> idsPage = discussionRepository.findIdsByAllTechnologies(uniqueIds, uniqueIds.size(), pageable);
            discussionsPage = idsPage.map(this::getDiscussionEntity);
        }
        return discussionsPage.map(d -> mapperService.toDiscussionResponse(d, (int) commentRepository.countByDiscussionId(d.getId())));
    }

    Discussion getDiscussionEntity(Long discussionId) {
        return discussionRepository.findDetailedById(discussionId)
                .orElseThrow(() -> new NotFoundException("Discussion not found"));
    }

    private void validateAuthor(Discussion discussion, Long currentUserId) {
        if (!discussion.getAuthor().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to modify this discussion");
        }
    }
}
