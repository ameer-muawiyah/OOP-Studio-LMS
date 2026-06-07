package com.oopstudio.lms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.oopstudio.lms.models.QuizResult;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

	@Query("select coalesce(avg(result.score), 0) from QuizResult result")
	Double calculateAverageScore();

	@Query("select coalesce(max(result.score), 0) from QuizResult result")
	Integer findMaximumScore();
}
