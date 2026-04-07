package com.DevLink.backend.repository;

import com.DevLink.backend.entity.Application;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByProjectIdAndApplicantId(Long projectId, Long applicantId);

    @EntityGraph(attributePaths = {"applicant", "applicant.technologies"})
    List<Application> findByProjectIdOrderByAppliedAtDesc(Long projectId);

    Optional<Application> findByProjectIdAndApplicantId(Long projectId, Long applicantId);
}
