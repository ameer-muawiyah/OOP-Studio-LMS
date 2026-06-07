package com.oopstudio.lms.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.oopstudio.lms.models.Question;
import com.oopstudio.lms.models.QuizResult;
import com.oopstudio.lms.models.User;
import com.oopstudio.lms.repositories.QuestionRepository;
import com.oopstudio.lms.repositories.QuizResultRepository;
import com.oopstudio.lms.repositories.UserRepository;
import com.oopstudio.lms.services.AdaptiveEngineService;
import com.oopstudio.lms.services.AdaptiveEngineService.AdaptiveQuestionResult;

@RestController
public class AdaptiveExamController {

	private static final String CURRENT_DIFFICULTY = "currentDifficulty";
	private static final String SERVED_IDS = "servedIds";
	private static final String ANSWERED_IDS = "answeredIds";
	private static final String ACTIVE_QUESTION_ID = "activeQuestionId";
	private static final String SCORE = "score";
	private static final String ANSWERED_COUNT = "answeredCount";
	private static final String EXAM_COMPLETE = "examComplete";
	private static final String RESULT_PERSISTED = "resultPersisted";
	private static final int MAX_QUESTIONS_PER_EXAM = 9;
	private static final Question.Difficulty INITIAL_DIFFICULTY = Question.Difficulty.MEDIUM;
	private static final Set<String> ALLOWED_OPTIONS = Set.of("A", "B", "C", "D");

	private final AdaptiveEngineService adaptiveEngineService;
	private final QuestionRepository questionRepository;
	private final QuizResultRepository quizResultRepository;
	private final UserRepository userRepository;

	public AdaptiveExamController(
			AdaptiveEngineService adaptiveEngineService,
			QuestionRepository questionRepository,
			QuizResultRepository quizResultRepository,
			UserRepository userRepository
	) {
		this.adaptiveEngineService = adaptiveEngineService;
		this.questionRepository = questionRepository;
		this.quizResultRepository = quizResultRepository;
		this.userRepository = userRepository;
	}

	@GetMapping("/api/exam/start")
	public ResponseEntity<?> startExam(
			Authentication authentication,
			HttpSession session
	) {
		User student = getAuthenticatedStudent(authentication);
		if (student == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
					"STUDENT_ACCOUNT_REQUIRED",
					"Sign in with a registered student email account before starting an adaptive exam."
			));
		}

		resetExamSession(session);

		return adaptiveEngineService.findQuestionForDifficulty(INITIAL_DIFFICULTY, List.of())
				.map(result -> {
					storeQuestionResult(session, result);
					return ResponseEntity.ok(new ExamQuestionResponse(
							"STARTED",
							"Adaptive MCQ exam session started.",
							toQuestionPayload(result.question()),
							INITIAL_DIFFICULTY.name(),
							0,
							0,
							MAX_QUESTIONS_PER_EXAM,
							false
					));
				})
				.orElseGet(() -> {
					session.setAttribute(EXAM_COMPLETE, true);
					return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ExamQuestionResponse(
							"QUESTION_POOL_EMPTY",
							"No MEDIUM questions are currently available to start the exam.",
							null,
							INITIAL_DIFFICULTY.name(),
							0,
							0,
							MAX_QUESTIONS_PER_EXAM,
							true
					));
				});
	}

	@PostMapping("/api/exam/submit")
	public ResponseEntity<?> submitAnswer(
			@RequestParam Long questionId,
			@RequestParam String selectedOption,
			Authentication authentication,
			HttpSession session
	) {
		User student = getAuthenticatedStudent(authentication);
		if (student == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
					"STUDENT_ACCOUNT_REQUIRED",
					"Sign in with a registered student email account before submitting an adaptive exam answer."
			));
		}

		Question.Difficulty currentDifficulty = getCurrentDifficulty(session);
		if (currentDifficulty == null) {
			return ResponseEntity.badRequest().body(new ErrorResponse(
					"EXAM_NOT_STARTED",
					"Start the exam before submitting an answer."
			));
		}

		if (isExamComplete(session)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
					"EXAM_ALREADY_COMPLETED",
					"The current adaptive exam session is already complete."
			));
		}

		Long activeQuestionId = getLongAttribute(session, ACTIVE_QUESTION_ID);
		if (!Objects.equals(activeQuestionId, questionId)) {
			return ResponseEntity.badRequest().body(new ErrorResponse(
					"QUESTION_NOT_ACTIVE",
					"Submitted questionId does not match the active session question."
			));
		}

		String normalizedOption;
		try {
			normalizedOption = normalizeSelectedOption(selectedOption);
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_OPTION", exception.getMessage()));
		}

		Question submittedQuestion = questionRepository.findById(questionId).orElse(null);
		if (submittedQuestion == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
					"QUESTION_NOT_FOUND",
					"No question exists for the submitted questionId."
			));
		}

		boolean isCorrect = isCorrectAnswer(submittedQuestion, normalizedOption);
		int updatedScore = getIntegerAttribute(session, SCORE) + (isCorrect ? 1 : 0);
		int updatedAnsweredCount = getIntegerAttribute(session, ANSWERED_COUNT) + 1;
		List<Long> updatedAnsweredIds = appendDistinctId(getSessionLongList(session, ANSWERED_IDS), questionId);

		session.setAttribute(SCORE, updatedScore);
		session.setAttribute(ANSWERED_COUNT, updatedAnsweredCount);
		session.setAttribute(ANSWERED_IDS, updatedAnsweredIds);

		Question.Difficulty nextDifficulty = adaptiveEngineService.calculateNextDifficulty(currentDifficulty, isCorrect);
		if (updatedAnsweredCount >= MAX_QUESTIONS_PER_EXAM) {
			markExamComplete(session, nextDifficulty, updatedScore, updatedAnsweredCount, student);
			return ResponseEntity.ok(buildCompletedSubmitResponse(
					isCorrect,
					updatedScore,
					updatedAnsweredCount,
					nextDifficulty,
					"QUESTION_LIMIT_REACHED",
					"Exam completed after reaching the configured question limit."
			));
		}

		List<Long> servedQuestionIds = getSessionLongList(session, SERVED_IDS);
		return adaptiveEngineService.findQuestionForDifficulty(nextDifficulty, servedQuestionIds)
				.map(result -> {
					storeQuestionResult(session, result);
					return ResponseEntity.ok(new SubmitAnswerResponse(
							"ANSWER_RECORDED",
							"Answer recorded and next adaptive question selected.",
							isCorrect,
							updatedScore,
							updatedAnsweredCount,
							MAX_QUESTIONS_PER_EXAM,
							result.difficulty().name(),
							toQuestionPayload(result.question()),
							false,
							null
					));
				})
				.orElseGet(() -> {
					markExamComplete(session, nextDifficulty, updatedScore, updatedAnsweredCount, student);
					return ResponseEntity.ok(buildCompletedSubmitResponse(
							isCorrect,
							updatedScore,
							updatedAnsweredCount,
							nextDifficulty,
							"QUESTION_POOL_EXHAUSTED",
							"No unserved question is available for the next adaptive difficulty."
					));
				});
	}

	@GetMapping("/api/exam/results")
	public ResponseEntity<ExamStatsResponse> examResults(HttpSession session) {
		return ResponseEntity.ok(buildStatsResponse(session));
	}

	private void resetExamSession(HttpSession session) {
		session.setAttribute(CURRENT_DIFFICULTY, INITIAL_DIFFICULTY);
		session.setAttribute(SERVED_IDS, List.<Long>of());
		session.setAttribute(ANSWERED_IDS, List.<Long>of());
		session.setAttribute(ACTIVE_QUESTION_ID, null);
		session.setAttribute(SCORE, 0);
		session.setAttribute(ANSWERED_COUNT, 0);
		session.setAttribute(EXAM_COMPLETE, false);
		session.setAttribute(RESULT_PERSISTED, false);
	}

	private void storeQuestionResult(HttpSession session, AdaptiveQuestionResult result) {
		session.setAttribute(CURRENT_DIFFICULTY, result.difficulty());
		session.setAttribute(SERVED_IDS, List.copyOf(result.servedQuestionIds()));
		session.setAttribute(ACTIVE_QUESTION_ID, result.question().getId());
		session.setAttribute(EXAM_COMPLETE, false);
	}

	private void markExamComplete(
			HttpSession session,
			Question.Difficulty finalDifficulty,
			int score,
			int answeredCount,
			User student
	) {
		session.setAttribute(CURRENT_DIFFICULTY, finalDifficulty);
		session.setAttribute(ACTIVE_QUESTION_ID, null);
		session.setAttribute(EXAM_COMPLETE, true);
		persistQuizResultIfNecessary(session, finalDifficulty, score, answeredCount, student);
	}

	private void persistQuizResultIfNecessary(
			HttpSession session,
			Question.Difficulty finalDifficulty,
			int score,
			int answeredCount,
			User student
	) {
		Object persisted = session.getAttribute(RESULT_PERSISTED);
		if (persisted instanceof Boolean alreadyPersisted && alreadyPersisted) {
			return;
		}

		QuizResult quizResult = new QuizResult();
		quizResult.setStudent(student);
		quizResult.setScore(score);
		quizResult.setTotalQuestions(answeredCount);
		quizResult.setDifficultyReached(finalDifficulty.name());
		quizResult.setCompletedAt(LocalDateTime.now());

		quizResultRepository.save(quizResult);
		session.setAttribute(RESULT_PERSISTED, true);
	}

	private SubmitAnswerResponse buildCompletedSubmitResponse(
			boolean isCorrect,
			int score,
			int answeredCount,
			Question.Difficulty finalDifficulty,
			String completionReason,
			String message
	) {
		return new SubmitAnswerResponse(
				"EXAM_COMPLETED",
				message,
				isCorrect,
				score,
				answeredCount,
				MAX_QUESTIONS_PER_EXAM,
				finalDifficulty.name(),
				null,
				true,
				completionReason
		);
	}

	private ExamStatsResponse buildStatsResponse(HttpSession session) {
		int score = getIntegerAttribute(session, SCORE);
		int answeredCount = getIntegerAttribute(session, ANSWERED_COUNT);
		double accuracyPercent = answeredCount == 0 ? 0.0 : Math.round((score * 10000.0) / answeredCount) / 100.0;
		Question.Difficulty currentDifficulty = getCurrentDifficulty(session);

		return new ExamStatsResponse(
				score,
				answeredCount,
				MAX_QUESTIONS_PER_EXAM,
				accuracyPercent,
				currentDifficulty == null ? null : currentDifficulty.name(),
				getSessionLongList(session, SERVED_IDS),
				getSessionLongList(session, ANSWERED_IDS),
				isExamComplete(session)
		);
	}

	private QuestionPayload toQuestionPayload(Question question) {
		return new QuestionPayload(
				question.getId(),
				question.getQuestionText(),
				question.getOptionA(),
				question.getOptionB(),
				question.getOptionC(),
				question.getOptionD(),
				question.getDifficulty().name(),
				question.getTopicTag()
		);
	}

	private String normalizeSelectedOption(String selectedOption) {
		if (selectedOption == null || selectedOption.isBlank()) {
			throw new IllegalArgumentException("selectedOption must be one of A, B, C, or D.");
		}

		String normalizedOption = selectedOption.trim().toUpperCase(Locale.ROOT);
		if (!ALLOWED_OPTIONS.contains(normalizedOption)) {
			throw new IllegalArgumentException("selectedOption must be one of A, B, C, or D.");
		}

		return normalizedOption;
	}

	private User getAuthenticatedStudent(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			return null;
		}

		return userRepository.findByEmail(authentication.getName())
				.filter(user -> user.getRole() == User.Role.STUDENT)
				.orElse(null);
	}

	private boolean isCorrectAnswer(Question question, String selectedOption) {
		return question.getCorrectOption() != null
				&& selectedOption.equalsIgnoreCase(question.getCorrectOption().toString());
	}

	private Question.Difficulty getCurrentDifficulty(HttpSession session) {
		Object value = session.getAttribute(CURRENT_DIFFICULTY);
		if (value instanceof Question.Difficulty difficulty) {
			return difficulty;
		}

		if (value instanceof String difficultyName) {
			return Question.Difficulty.valueOf(difficultyName);
		}

		return null;
	}

	private int getIntegerAttribute(HttpSession session, String attributeName) {
		Object value = session.getAttribute(attributeName);
		return value instanceof Integer integerValue ? integerValue : 0;
	}

	private Long getLongAttribute(HttpSession session, String attributeName) {
		Object value = session.getAttribute(attributeName);
		return value instanceof Long longValue ? longValue : null;
	}

	private List<Long> getSessionLongList(HttpSession session, String attributeName) {
		Object value = session.getAttribute(attributeName);
		if (value instanceof List<?> rawList) {
			return rawList.stream()
					.filter(Long.class::isInstance)
					.map(Long.class::cast)
					.toList();
		}

		return List.of();
	}

	private List<Long> appendDistinctId(List<Long> currentIds, Long id) {
		List<Long> updatedIds = new ArrayList<>(currentIds);
		if (id != null && !updatedIds.contains(id)) {
			updatedIds.add(id);
		}

		return List.copyOf(updatedIds);
	}

	private boolean isExamComplete(HttpSession session) {
		Object value = session.getAttribute(EXAM_COMPLETE);
		return value instanceof Boolean complete && complete;
	}

	public record ExamQuestionResponse(
			String status,
			String message,
			QuestionPayload question,
			String currentDifficulty,
			int score,
			int answeredCount,
			int maxQuestions,
			boolean completed
	) {
	}

	public record SubmitAnswerResponse(
			String status,
			String message,
			boolean correct,
			int score,
			int answeredCount,
			int maxQuestions,
			String currentDifficulty,
			QuestionPayload nextQuestion,
			boolean completed,
			String completionReason
	) {
	}

	public record ExamStatsResponse(
			int score,
			int answeredCount,
			int maxQuestions,
			double accuracyPercent,
			String currentDifficulty,
			List<Long> servedIds,
			List<Long> answeredIds,
			boolean completed
	) {

		public ExamStatsResponse {
			servedIds = List.copyOf(servedIds);
			answeredIds = List.copyOf(answeredIds);
		}
	}

	public record QuestionPayload(
			Long id,
			String questionText,
			String optionA,
			String optionB,
			String optionC,
			String optionD,
			String difficulty,
			String topicTag
	) {
	}

	public record ErrorResponse(String code, String message) {
	}
}
