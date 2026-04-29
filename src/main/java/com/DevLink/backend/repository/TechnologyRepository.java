package com.DevLink.backend.repository;

import com.DevLink.backend.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TechnologyRepository extends JpaRepository<Technology, Integer> {
    List<Technology> findByIdIn(List<Integer> ids);

    @Query(value = """
            SELECT t.id, t.name,
                   COALESCE(pt.cnt, 0) AS project_count,
                   COALESCE(ut.cnt, 0) AS user_count
            FROM technologies t
            LEFT JOIN (SELECT technology_id, COUNT(*) AS cnt FROM project_technologies GROUP BY technology_id) pt ON pt.technology_id = t.id
            LEFT JOIN (SELECT technology_id, COUNT(*) AS cnt FROM user_technologies GROUP BY technology_id) ut ON ut.technology_id = t.id
            ORDER BY (COALESCE(pt.cnt, 0) + COALESCE(ut.cnt, 0)) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findMostUsedTechnologiesRaw();
}
