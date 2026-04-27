package com.DevLink.backend.service;

import com.DevLink.backend.dto.StatisticsResponse;
import com.DevLink.backend.dto.TechnologyStatResponse;
import com.DevLink.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final DiscussionRepository discussionRepository;
    private final CommentRepository commentRepository;
    private final TechnologyRepository technologyRepository;

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics() {
        long totalUsers = userRepository.count();
        long totalProjects = projectRepository.count();
        long totalDiscussions = discussionRepository.count();
        long totalComments = commentRepository.count();

        List<TechnologyStatResponse> mostUsed = technologyRepository.findMostUsedTechnologiesRaw().stream()
                .map(row -> new TechnologyStatResponse(
                        ((Number) row[0]).intValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()
                ))
                .toList();

        return new StatisticsResponse(totalUsers, totalProjects, totalDiscussions, totalComments, mostUsed);
    }
}
