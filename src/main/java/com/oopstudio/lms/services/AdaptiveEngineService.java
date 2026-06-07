package com.oopstudio.lms.services;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.oopstudio.lms.models.Question;
import com.oopstudio.lms.repositories.QuestionRepository;

@Service
public class AdaptiveEngineService {

	private static final Long EMPTY_EXCLUSION_SENTINEL_ID = -1L;

	private final QuestionRepository questionRepository;

	public AdaptiveEngineService(QuestionRepository questionRepository) {
		this.questionRepository = questionRepository;
	}

	public AdaptiveQuestionResult getNextQuestion(
			Question.Difficulty currentDifficulty,
			boolean isCorrect,
			List<Long> servedQuestionIds
	) {
		Question.Difficulty nextDifficulty = calculateNextDifficulty(currentDifficulty, isCorrect);

		return findQuestionForDifficulty(nextDifficulty, servedQuestionIds)
				.orElseThrow(() -> new NoSuchElementException(
						"No unserved question is available for difficulty " + nextDifficulty
				));
	}

	public Optional<AdaptiveQuestionResult> findQuestionForDifficulty(
			Question.Difficulty difficulty,
			List<Long> servedQuestionIds
	) {
		Objects.requireNonNull(difficulty, "difficulty must not be null");

		List<Long> normalizedServedIds = normalizeServedQuestionIds(servedQuestionIds);
		List<Long> excludedQuestionIds = normalizedServedIds.isEmpty()
				? List.of(EMPTY_EXCLUSION_SENTINEL_ID)
				: normalizedServedIds;

		return questionRepository.findRandomQuestionByDifficultyExcludingIds(difficulty.name(), excludedQuestionIds)
				.map(question -> new AdaptiveQuestionResult(
						question,
						difficulty,
						appendServedQuestionId(normalizedServedIds, question.getId())
				));
	}

	public Question.Difficulty calculateNextDifficulty(Question.Difficulty currentDifficulty, boolean isCorrect) {
		Objects.requireNonNull(currentDifficulty, "currentDifficulty must not be null");

		if (isCorrect) {
			return switch (currentDifficulty) {
				case EASY -> Question.Difficulty.MEDIUM;
				case MEDIUM, HARD -> Question.Difficulty.HARD;
			};
		}

		return switch (currentDifficulty) {
			case HARD -> Question.Difficulty.MEDIUM;
			case MEDIUM, EASY -> Question.Difficulty.EASY;
		};
	}

	private List<Long> normalizeServedQuestionIds(List<Long> servedQuestionIds) {
		if (servedQuestionIds == null || servedQuestionIds.isEmpty()) {
			return List.of();
		}

		return servedQuestionIds.stream()
				.filter(Objects::nonNull)
				.distinct()
				.toList();
	}

	private List<Long> appendServedQuestionId(List<Long> servedQuestionIds, Long questionId) {
		if (questionId == null) {
			return servedQuestionIds;
		}

		return Stream.concat(servedQuestionIds.stream(), Stream.of(questionId))
				.distinct()
				.toList();
	}

	public record AdaptiveQuestionResult(
			Question question,
			Question.Difficulty difficulty,
			List<Long> servedQuestionIds
	) {

		public AdaptiveQuestionResult {
			Objects.requireNonNull(question, "question must not be null");
			Objects.requireNonNull(difficulty, "difficulty must not be null");
			servedQuestionIds = List.copyOf(Objects.requireNonNull(
					servedQuestionIds,
					"servedQuestionIds must not be null"
			));
		}
	}
}
