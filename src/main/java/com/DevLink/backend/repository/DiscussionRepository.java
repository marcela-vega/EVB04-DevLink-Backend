package com.DevLink.backend.repository;

import com.DevLink.backend.entity.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    @EntityGraph(attributePaths = {"author", "technologies"})
    Optional<Discussion> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"author", "technologies"})
    Page<Discussion> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "technologies"})
    Page<Discussion> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    @Query(value = """
            SELECT dt.discussion_id
            FROM discussion_technologies dt
            JOIN discussions d ON d.id = dt.discussion_id
            WHERE dt.technology_id IN (:technologyIds)
            GROUP BY dt.discussion_id
            HAVING COUNT(DISTINCT dt.technology_id) = :technologyCount
            ORDER BY MAX(d.created_at) DESC
            """, countQuery = """
            SELECT COUNT(DISTINCT dt.discussion_id)
            FROM discussion_technologies dt
            JOIN discussions d ON d.id = dt.discussion_id
            WHERE dt.technology_id IN (:technologyIds)
            GROUP BY dt.discussion_id
            HAVING COUNT(DISTINCT dt.technology_id) = :technologyCount
            """, nativeQuery = true)
    Page<Long> findIdsByAllTechnologies(
            @Param("technologyIds") List<Integer> technologyIds,
            @Param("technologyCount") long technologyCount,
            Pageable pageable
    );
}
