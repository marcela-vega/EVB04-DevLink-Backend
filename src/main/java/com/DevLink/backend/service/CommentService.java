package com.DevLink.backend.service;

import com.DevLink.backend.dto.*;
import com.DevLink.backend.entity.Comment;
import com.DevLink.backend.entity.Discussion;
import com.DevLink.backend.entity.User;
import com.DevLink.backend.entity.enums.NotificationType;
import com.DevLink.backend.exception.NotFoundException;
import com.DevLink.backend.exception.UnauthorizedException;
import com.DevLink.backend.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final DiscussionService discussionService;
    private final UserService userService;
    private final MapperService mapperService;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse create(Long discussionId, String email, CreateCommentRequest request) {
        User author = userService.getCurrentUserEntity(email);
        Discussion discussion = discussionService.getDiscussionEntity(discussionId);
        Comment comment = Comment.builder()
                .author(author)
                .discussion(discussion)
                .content(request.content().trim())
                .build();
        Comment saved = commentRepository.save(comment);
        if (!discussion.getAuthor().getId().equals(author.getId())) {
            notificationService.create(discussion.getAuthor(),
                    "New comment on your discussion",
                    author.getFullName() + " commented on '" + discussion.getTitle() + "'.",
                    NotificationType.NEW_COMMENT);
        }
        return mapperService.toCommentResponse(saved);
    }

    @Transactional
    public CommentResponse update(Long commentId, String email, UpdateCommentRequest request) {
        User currentUser = userService.getCurrentUserEntity(email);
        Comment comment = getCommentEntity(commentId);
        validateAuthor(comment, currentUser.getId());
        comment.setContent(request.content().trim());
        Comment saved = commentRepository.save(comment);
        return mapperService.toCommentResponse(saved);
    }

    @Transactional
    public ApiMessageResponse delete(Long commentId, String email) {
        User currentUser = userService.getCurrentUserEntity(email);
        Comment comment = getCommentEntity(commentId);
        validateAuthor(comment, currentUser.getId());
        commentRepository.delete(comment);
        return new ApiMessageResponse("Comment deleted successfully");
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getByDiscussion(Long discussionId) {
        discussionService.getDiscussionEntity(discussionId);
        return commentRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId)
                .stream()
                .map(mapperService::toCommentResponse)
                .toList();
    }

    private Comment getCommentEntity(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
    }

    private void validateAuthor(Comment comment, Long currentUserId) {
        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to modify this comment");
        }
    }
}
