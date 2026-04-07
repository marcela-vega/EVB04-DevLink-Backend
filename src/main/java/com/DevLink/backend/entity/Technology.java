package com.DevLink.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "technologies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Technology {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;
}
