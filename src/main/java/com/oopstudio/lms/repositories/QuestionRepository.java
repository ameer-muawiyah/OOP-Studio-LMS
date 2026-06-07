package com.oopstudio.lms.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.oopstudio.lms.models.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

	@Query(value = """
			SELECT *
			FROM questions q
			WHERE q.difficulty = :difficulty
			  AND q.id NOT IN (:excludedQuestionIds)
			ORDER BY RAND()
			LIMIT 1
			""", nativeQuery = true)
	Optional<Question> findRandomQuestionByDifficultyExcludingIds(
			@Param("difficulty") String difficulty,
			@Param("excludedQuestionIds") List<Long> excludedQuestionIds
	);
}
