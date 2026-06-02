package com.DevLink.backend.repository;

import com.DevLink.backend.entity.Application;
import com.DevLink.backend.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByProjectIdAndApplicantId(Long projectId, Long applicantId);

    boolean existsByProjectIdAndApplicantIdAndStatusNot(Long projectId, Long applicantId, ApplicationStatus status);

    @EntityGraph(attributePaths = {"project", "project.creator", "project.technologies"})
    List<Application> findByApplicantIdAndStatus(Long applicantId, ApplicationStatus status);

    @EntityGraph(attributePaths = {"applicant", "applicant.technologies"})
    List<Application> findByProjectIdOrderByAppliedAtDesc(Long projectId);

    Optional<Application> findByProjectIdAndApplicantId(Long projectId, Long applicantId);

    long countByApplicantIdAndStatus(Long applicantId, ApplicationStatus status);

    long countByProjectId(Long projectId);

    boolean existsByProjectIdAndStatus(Long projectId, ApplicationStatus status);

    @EntityGraph(attributePaths = {"applicant", "applicant.technologies"})
    List<Application> findByProjectIdAndStatusOrderByAppliedAtAsc(Long projectId, ApplicationStatus status);
}
