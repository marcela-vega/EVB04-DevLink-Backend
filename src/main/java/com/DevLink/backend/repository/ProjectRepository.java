package com.DevLink.backend.repository;

import com.DevLink.backend.entity.Project;
import com.DevLink.backend.entity.enums.ProjectStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @EntityGraph(attributePaths = {"creator", "technologies"})
    Optional<Project> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"creator", "technologies"})
    List<Project> findByStatusOrderByCreatedAtDesc(ProjectStatus status);

    @Query(value = """
            select pt.project_id
            from project_technologies pt
            join projects p on p.id = pt.project_id
            where p.status = :status and pt.technology_id in (:technologyIds)
            group by pt.project_id
            having count(distinct pt.technology_id) = :technologyCount
            order by max(p.created_at) desc
            """, nativeQuery = true)
    List<Long> findPublishedIdsByAllTechnologies(
            @Param("status") String status,
            @Param("technologyIds") List<Integer> technologyIds,
            @Param("technologyCount") long technologyCount
    );
}
