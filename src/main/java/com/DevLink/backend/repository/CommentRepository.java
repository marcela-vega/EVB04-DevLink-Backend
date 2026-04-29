package com.DevLink.backend.repository;

import com.DevLink.backend.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author"})
    List<Comment> findByDiscussionIdOrderByCreatedAtAsc(Long discussionId);

    long countByDiscussionId(Long discussionId);
}
