package com.oopstudio.lms.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oopstudio.lms.models.ExamSession;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

	Optional<ExamSession> findFirstByOrderByIdAsc();
}
