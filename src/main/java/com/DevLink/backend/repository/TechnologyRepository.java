package com.DevLink.backend.repository;

import com.DevLink.backend.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TechnologyRepository extends JpaRepository<Technology, Integer> {
    List<Technology> findByIdIn(List<Integer> ids);
}
