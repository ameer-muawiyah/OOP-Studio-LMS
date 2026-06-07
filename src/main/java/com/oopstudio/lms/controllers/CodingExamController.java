package com.oopstudio.lms.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.oopstudio.lms.models.CodingQuestion;
import com.oopstudio.lms.repositories.CodingQuestionRepository;
import com.oopstudio.lms.services.CodeExecutionService;
import com.oopstudio.lms.services.CodeExecutionService.CodeExecutionResult;

@RestController
public class CodingExamController {

	private final CodingQuestionRepository codingQuestionRepository;
	private final CodeExecutionService codeExecutionService;

	public CodingExamController(
			CodingQuestionRepository codingQuestionRepository,
			CodeExecutionService codeExecutionService
	) {
		this.codingQuestionRepository = codingQuestionRepository;
		this.codeExecutionService = codeExecutionService;
	}

	@GetMapping("/api/compiler/challenge/{id}")
	public ResponseEntity<?> fetchChallenge(@PathVariable Long id) {
		return codingQuestionRepository.findById(id)
				.<ResponseEntity<?>>map(question -> ResponseEntity.ok(toChallengePayload(question)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
						"CHALLENGE_NOT_FOUND",
						"No coding challenge exists for id " + id + "."
				)));
	}

	@GetMapping("/api/compiler/challenges")
	public List<ChallengeSummary> fetchChallenges() {
		return codingQuestionRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
				.map(question -> new ChallengeSummary(
						question.getId(),
						question.getTitle(),
						question.getDifficulty().name()
				))
				.toList();
	}

	@PostMapping("/api/compiler/run")
	public ResponseEntity<?> runCode(@RequestBody Map<String, Object> requestBody) {
		Long questionId;
		try {
			questionId = parseQuestionId(requestBody.get("questionId"));
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_QUESTION_ID", exception.getMessage()));
		}

		String studentSourceCode = parseStudentSourceCode(requestBody.get("studentSourceCode"));
		if (studentSourceCode == null || studentSourceCode.isBlank()) {
			return ResponseEntity.badRequest().body(new ErrorResponse(
					"SOURCE_CODE_REQUIRED",
					"studentSourceCode must not be blank."
			));
		}

		return codingQuestionRepository.findById(questionId)
				.<ResponseEntity<?>>map(question -> {
					CodeExecutionResult executionResult = codeExecutionService.executeCode(
							studentSourceCode,
							question.getTestCasesJson()
					);
					return ResponseEntity.ok(new RunCodeResponse(
							question.getId(),
							question.getTitle(),
							executionResult
					));
				})
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
						"CHALLENGE_NOT_FOUND",
						"No coding challenge exists for id " + questionId + "."
				)));
	}

	private ChallengePayload toChallengePayload(CodingQuestion question) {
		return new ChallengePayload(
				question.getId(),
				question.getTitle(),
				question.getDescription(),
				question.getDifficulty().name()
		);
	}

	private Long parseQuestionId(Object value) {
		if (value instanceof Number number) {
			return number.longValue();
		}

		if (value instanceof String stringValue && !stringValue.isBlank()) {
			try {
				return Long.parseLong(stringValue.trim());
			} catch (NumberFormatException exception) {
				throw new IllegalArgumentException("questionId must be a numeric value.");
			}
		}

		throw new IllegalArgumentException("questionId is required.");
	}

	private String parseStudentSourceCode(Object value) {
		return value instanceof String stringValue ? stringValue : null;
	}

	public record ChallengePayload(
			Long id,
			String title,
			String description,
			String difficulty
	) {
	}

	public record ChallengeSummary(
			Long id,
			String title,
			String difficulty
	) {
	}

	public record RunCodeResponse(
			Long questionId,
			String title,
			CodeExecutionResult result
	) {
	}

	public record ErrorResponse(String code, String message) {
	}
}
