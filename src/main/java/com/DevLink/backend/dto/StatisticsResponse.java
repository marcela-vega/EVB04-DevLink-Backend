package com.DevLink.backend.dto;

import java.util.List;

public record StatisticsResponse(
        long totalUsers,
        long totalProjects,
        long totalDiscussions,
        long totalComments,
        List<TechnologyStatResponse> mostUsedTechnologies
) {}
