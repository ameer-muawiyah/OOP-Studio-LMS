package com.oopstudio.lms.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exams")
public class Exam {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(nullable = false)
	private Integer durationMinutes;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TestType testType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private QuestionStrategy questionStrategy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ExamStatus status;

	public Exam() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public TestType getTestType() {
		return testType;
	}

	public void setTestType(TestType testType) {
		this.testType = testType;
	}

	public QuestionStrategy getQuestionStrategy() {
		return questionStrategy;
	}

	public void setQuestionStrategy(QuestionStrategy questionStrategy) {
		this.questionStrategy = questionStrategy;
	}

	public ExamStatus getStatus() {
		return status;
	}

	public void setStatus(ExamStatus status) {
		this.status = status;
	}

	public enum TestType {
		MCQ,
		CODING
	}

	public enum QuestionStrategy {
		TEACHER_CUSTOM,
		SYSTEM_ADAPTIVE
	}

	public enum ExamStatus {
		PENDING,
		ACTIVE,
		COMPLETED
	}
}
