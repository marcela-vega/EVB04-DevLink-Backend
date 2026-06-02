package com.DevLink.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_security_answers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSecurityAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private SecurityQuestion question;

    // BCrypt hash of the normalised answer
    @Column(name = "answer_hash", nullable = false, length = 255)
    private String answerHash;
}
