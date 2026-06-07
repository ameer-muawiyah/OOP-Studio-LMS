package com.oopstudio.lms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oopstudio.lms.models.CodingQuestion;

@Repository
public interface CodingQuestionRepository extends JpaRepository<CodingQuestion, Long> {
}
