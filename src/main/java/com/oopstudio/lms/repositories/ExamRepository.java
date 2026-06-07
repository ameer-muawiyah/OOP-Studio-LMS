package com.oopstudio.lms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oopstudio.lms.models.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
}
