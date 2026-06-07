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
@Table(name = "questions")
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String questionText;

	@Column(name = "option_a", nullable = false)
	private String optionA;

	@Column(name = "option_b", nullable = false)
	private String optionB;

	@Column(name = "option_c", nullable = false)
	private String optionC;

	@Column(name = "option_d", nullable = false)
	private String optionD;

	@Column(nullable = false, length = 1)
	private Character correctOption;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Difficulty difficulty;

	@Column(nullable = false, length = 120)
	private String topicTag;

	public Question() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

	public String getOptionA() {
		return optionA;
	}

	public void setOptionA(String optionA) {
		this.optionA = optionA;
	}

	public String getOptionB() {
		return optionB;
	}

	public void setOptionB(String optionB) {
		this.optionB = optionB;
	}

	public String getOptionC() {
		return optionC;
	}

	public void setOptionC(String optionC) {
		this.optionC = optionC;
	}

	public String getOptionD() {
		return optionD;
	}

	public void setOptionD(String optionD) {
		this.optionD = optionD;
	}

	public Character getCorrectOption() {
		return correctOption;
	}

	public void setCorrectOption(Character correctOption) {
		this.correctOption = correctOption;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public String getTopicTag() {
		return topicTag;
	}

	public void setTopicTag(String topicTag) {
		this.topicTag = topicTag;
	}

	public enum Difficulty {
		EASY,
		MEDIUM,
		HARD
	}
}
