package com.DevLink.backend.dto;

public record TechnologyStatResponse(
        Integer id,
        String name,
        long projectCount,
        long userCount
) {}
