package com.oopstudio.lms.controllers;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oopstudio.lms.models.ExamSession;
import com.oopstudio.lms.models.QuizResult;
import com.oopstudio.lms.repositories.QuizResultRepository;
import com.oopstudio.lms.services.ExamSessionService;

@Controller
public class InstructorController {

	private final QuizResultRepository quizResultRepository;
	private final ExamSessionService examSessionService;

	public InstructorController(
			QuizResultRepository quizResultRepository,
			ExamSessionService examSessionService
	) {
		this.quizResultRepository = quizResultRepository;
		this.examSessionService = examSessionService;
	}

	@GetMapping("/instructor/dashboard")
	public String dashboard(Model model) {
		List<QuizResult> results = quizResultRepository.findAll(Sort.by(Sort.Direction.DESC, "completedAt"));
		Double averageScore = quizResultRepository.calculateAverageScore();
		Integer highestScore = quizResultRepository.findMaximumScore();
		ExamSession examSession = examSessionService.getOrCreateExamSession();

		model.addAttribute("results", results);
		model.addAttribute("totalExams", results.size());
		model.addAttribute("averageScore", formatScore(averageScore));
		model.addAttribute("highestScore", highestScore == null ? 0 : highestScore);
		model.addAttribute("examActive", Boolean.TRUE.equals(examSession.getActive()));

		return "instructor-dashboard";
	}

	@PostMapping("/api/instructor/exam/toggle")
	@ResponseBody
	public ResponseEntity<ExamControlResponse> toggleOfficialExam() {
		ExamSession examSession = examSessionService.toggleOfficialExam();
		return ResponseEntity.ok(new ExamControlResponse(
				Boolean.TRUE.equals(examSession.getActive()),
				examSession.getUpdatedAt().toString()
		));
	}

	private String formatScore(Double score) {
		double safeScore = score == null ? 0.0 : score;
		return String.format(Locale.ROOT, "%.2f", safeScore);
	}

	public record ExamControlResponse(boolean active, String updatedAt) {
	}
}
