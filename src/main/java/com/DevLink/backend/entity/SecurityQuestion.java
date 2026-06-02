package com.DevLink.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "security_questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SecurityQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String question;
}
