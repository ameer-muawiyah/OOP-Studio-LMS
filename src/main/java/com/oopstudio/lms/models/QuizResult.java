package com.oopstudio.lms.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_results")
public class QuizResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String studentName;

	@Column(nullable = false)
	private Integer score;

	@Column(nullable = false)
	private Integer totalQuestions;

	@Column(nullable = false, length = 40)
	private String difficultyReached;

	@Column(nullable = false)
	private LocalDateTime completedAt;

	public QuizResult() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Integer getTotalQuestions() {
		return totalQuestions;
	}

	public void setTotalQuestions(Integer totalQuestions) {
		this.totalQuestions = totalQuestions;
	}

	public String getDifficultyReached() {
		return difficultyReached;
	}

	public void setDifficultyReached(String difficultyReached) {
		this.difficultyReached = difficultyReached;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}
}
