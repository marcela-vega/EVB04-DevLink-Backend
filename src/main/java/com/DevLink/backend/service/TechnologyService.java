package com.DevLink.backend.service;

import com.DevLink.backend.dto.TechnologyResponse;
import com.DevLink.backend.entity.Technology;
import com.DevLink.backend.exception.BadRequestException;
import com.DevLink.backend.repository.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TechnologyService {
    private final TechnologyRepository technologyRepository;
    private final MapperService mapperService;

    @Transactional(readOnly = true)
    public List<Technology> getTechnologiesByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("At least one technology must be selected");
        }

        Set<Integer> uniqueIds = new HashSet<>(ids);
        List<Technology> technologies = technologyRepository.findByIdIn(uniqueIds.stream().toList());
        if (technologies.size() != uniqueIds.size()) {
            throw new BadRequestException("One or more technologies do not exist");
        }
        return technologies;
    }

    @Transactional(readOnly = true)
    public List<TechnologyResponse> getAll() {
        return technologyRepository.findAll().stream()
                .sorted(Comparator.comparing(Technology::getName))
                .map(mapperService::toTechnologyResponse)
                .toList();
    }
}
