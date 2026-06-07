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
@Table(name = "coding_questions")
public class CodingQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 180)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Difficulty difficulty;

	@Column(name = "test_cases_json", nullable = false, columnDefinition = "TEXT")
	private String testCasesJson;

	public CodingQuestion() {
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public String getTestCasesJson() {
		return testCasesJson;
	}

	public void setTestCasesJson(String testCasesJson) {
		this.testCasesJson = testCasesJson;
	}

	public enum Difficulty {
		EASY,
		MEDIUM,
		HARD
	}
}
