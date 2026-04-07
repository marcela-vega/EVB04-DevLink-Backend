package com.DevLink.backend.controller;

import com.DevLink.backend.dto.TechnologyResponse;
import com.DevLink.backend.service.TechnologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/technologies")
@RequiredArgsConstructor
public class TechnologyController {
    private final TechnologyService technologyService;

    @GetMapping
    public ResponseEntity<List<TechnologyResponse>> getAll() {
        return ResponseEntity.ok(technologyService.getAll());
    }
}
