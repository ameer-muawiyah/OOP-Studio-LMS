package com.oopstudio.lms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.oopstudio.lms.models.QuizResult;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

	@Query("select coalesce(avg(result.score), 0) from QuizResult result")
	Double calculateAverageScore();

	@Query("select coalesce(max(result.score), 0) from QuizResult result")
	Integer findMaximumScore();

	List<QuizResult> findByStudentSupervisorIdOrderByCompletedAtDesc(Long supervisorId);

	List<QuizResult> findTop10ByStudentIdOrderByIdDesc(Long studentId);

	@Query("""
			select coalesce(avg(result.score), 0)
			from QuizResult result
			where result.student.supervisor.id = :supervisorId
			""")
	Double calculateAverageScoreForSupervisor(@Param("supervisorId") Long supervisorId);

	@Query("""
			select coalesce(max(result.score), 0)
			from QuizResult result
			where result.student.supervisor.id = :supervisorId
			""")
	Integer findMaximumScoreForSupervisor(@Param("supervisorId") Long supervisorId);
}
