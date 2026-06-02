package com.DevLink.backend.repository;

import com.DevLink.backend.entity.UserSecurityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSecurityAnswerRepository extends JpaRepository<UserSecurityAnswer, Long> {

    @Query("SELECT a FROM UserSecurityAnswer a JOIN FETCH a.question WHERE a.user.id = :userId")
    List<UserSecurityAnswer> findWithQuestionByUserId(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserSecurityAnswer a WHERE a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
