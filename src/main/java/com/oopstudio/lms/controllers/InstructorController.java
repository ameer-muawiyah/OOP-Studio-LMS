package com.oopstudio.lms.controllers;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import com.oopstudio.lms.models.QuizResult;
import com.oopstudio.lms.models.User;
import com.oopstudio.lms.repositories.QuizResultRepository;
import com.oopstudio.lms.repositories.UserRepository;

@Controller
public class InstructorController {

	private final QuizResultRepository quizResultRepository;
	private final UserRepository userRepository;

	public InstructorController(
			QuizResultRepository quizResultRepository,
			UserRepository userRepository
	) {
		this.quizResultRepository = quizResultRepository;
		this.userRepository = userRepository;
	}

	@GetMapping("/instructor/dashboard")
	public String dashboard(
			Authentication authentication,
			Model model
	) {
		User teacher = resolveAuthenticatedTeacher(authentication);
		List<QuizResult> results = quizResultRepository.findByStudentSupervisorIdOrderByCompletedAtDesc(teacher.getId());
		Double averageScore = quizResultRepository.calculateAverageScoreForSupervisor(teacher.getId());
		Integer highestScore = quizResultRepository.findMaximumScoreForSupervisor(teacher.getId());

		model.addAttribute("results", results);
		model.addAttribute("totalExams", results.size());
		model.addAttribute("averageScore", formatScore(averageScore));
		model.addAttribute("highestScore", highestScore == null ? 0 : highestScore);
		model.addAttribute("teacherName", buildTeacherName(teacher));

		return "instructor-dashboard";
	}

	private String formatScore(Double score) {
		double safeScore = score == null ? 0.0 : score;
		return String.format(Locale.ROOT, "%.2f", safeScore);
	}

	private User resolveAuthenticatedTeacher(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher authentication is required.");
		}

		return userRepository.findByUniqueTeacherId(authentication.getName())
				.filter(user -> user.getRole() == User.Role.TEACHER)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher account is required."));
	}

	private String buildTeacherName(User teacher) {
		String firstName = teacher.getFirstName() == null ? "" : teacher.getFirstName().trim();
		String lastName = teacher.getLastName() == null ? "" : teacher.getLastName().trim();
		String fullName = (firstName + " " + lastName).trim();
		return fullName.isBlank() ? teacher.getUniqueTeacherId() : fullName;
	}
}
