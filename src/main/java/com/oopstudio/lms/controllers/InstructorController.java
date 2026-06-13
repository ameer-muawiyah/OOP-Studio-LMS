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
		List<User> assignedStudents = userRepository.findBySupervisorId(teacher.getId());
		Double averageScore = quizResultRepository.calculateAverageScoreForSupervisor(teacher.getId());
		Integer highestScore = quizResultRepository.findMaximumScoreForSupervisor(teacher.getId());

		model.addAttribute("results", results);
		model.addAttribute("totalExams", results.size());
		model.addAttribute("averageScore", formatScore(averageScore));
		model.addAttribute("highestScore", highestScore == null ? 0 : highestScore);
		model.addAttribute("teacherName", buildTeacherName(teacher));
		model.addAttribute("teacherId", teacher.getUniqueTeacherId());
		model.addAttribute("rosterStudents", buildRosterRows(assignedStudents));

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

	private List<StudentRosterRow> buildRosterRows(List<User> assignedStudents) {
		return assignedStudents.stream()
				.map(student -> {
					List<QuizResult> recentResults = quizResultRepository.findTop10ByStudentIdOrderByIdDesc(student.getId());
					return new StudentRosterRow(
							buildStudentName(student),
							student.getEmail(),
							recentResults.size(),
							formatRollingAccuracy(recentResults)
					);
				})
				.toList();
	}

	private String buildStudentName(User student) {
		String firstName = student.getFirstName() == null ? "" : student.getFirstName().trim();
		String lastName = student.getLastName() == null ? "" : student.getLastName().trim();
		String fullName = (firstName + " " + lastName).trim();
		return fullName.isBlank() ? student.getEmail() : fullName;
	}

	private String formatRollingAccuracy(List<QuizResult> recentResults) {
		int totalQuestions = recentResults.stream()
				.map(QuizResult::getTotalQuestions)
				.filter(java.util.Objects::nonNull)
				.mapToInt(Integer::intValue)
				.sum();
		if (totalQuestions == 0) {
			return "No sessions recorded";
		}

		int totalScore = recentResults.stream()
				.map(QuizResult::getScore)
				.filter(java.util.Objects::nonNull)
				.mapToInt(Integer::intValue)
				.sum();
		double rollingAccuracy = (totalScore * 100.0) / totalQuestions;
		return String.format(Locale.ROOT, "%.1f%%", rollingAccuracy);
	}

	public record StudentRosterRow(
			String fullName,
			String email,
			int attemptCount,
			String rollingAccuracy
	) {
	}
}
