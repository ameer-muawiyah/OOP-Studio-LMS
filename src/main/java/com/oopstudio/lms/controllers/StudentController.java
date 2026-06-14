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
public class StudentController {

	private final UserRepository userRepository;
	private final QuizResultRepository quizResultRepository;

	public StudentController(
			UserRepository userRepository,
			QuizResultRepository quizResultRepository
	) {
		this.userRepository = userRepository;
		this.quizResultRepository = quizResultRepository;
	}

	@GetMapping("/student/dashboard")
	public String dashboard(
			Authentication authentication,
			Model model
	) {
		User student = resolveAuthenticatedStudent(authentication);

		// Supervisor name — LEFT JOIN FETCH means supervisor is already initialised
		User supervisor = student.getSupervisor();
		String supervisorName = (supervisor != null)
				? buildDisplayName(supervisor, supervisor.getUniqueTeacherId())
				: "Pending Assignment";

		// Recent exam history — top 5 most-recent results for this student
		List<QuizResult> recentResults = quizResultRepository
				.findTop5ByStudentIdOrderByCompletedAtDesc(student.getId());

		// Compute a simple overall accuracy percentage across all fetched results
		String overallAccuracy = computeAccuracy(recentResults);

		model.addAttribute("studentName", buildDisplayName(student, student.getEmail()));
		model.addAttribute("supervisorName", supervisorName);
		model.addAttribute("supervisorPending", supervisor == null);
		model.addAttribute("recentResults", recentResults);
		model.addAttribute("overallAccuracy", overallAccuracy);
		model.addAttribute("hasResults", !recentResults.isEmpty());

		return "student-dashboard";
	}

	// ── Private helpers ─────────────────────────────────────────────────────────

	/**
	 * Resolves the authenticated student from the Security context.
	 * For STUDENT accounts the principal name is the email address
	 * (set by CustomUserDetailsService#resolvePrincipalName).
	 * Uses a JOIN FETCH query so the lazy supervisor relation is
	 * initialised within the JPA transaction, avoiding any
	 * LazyInitializationException in the controller layer.
	 */
	private User resolveAuthenticatedStudent(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, "Student authentication is required.");
		}

		return userRepository.findByEmailWithSupervisor(authentication.getName())
				.filter(user -> user.getRole() == User.Role.STUDENT)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.FORBIDDEN, "Student account is required."));
	}

	/**
	 * Builds a human-readable display name from first + last name,
	 * falling back to the provided {@code fallback} if the name is blank.
	 */
	private String buildDisplayName(User user, String fallback) {
		String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
		String lastName  = user.getLastName()  == null ? "" : user.getLastName().trim();
		String fullName  = (firstName + " " + lastName).trim();
		return fullName.isBlank() ? fallback : fullName;
	}

	/**
	 * Computes an accuracy percentage string across the provided results.
	 * Returns "—" when there are no results to avoid a divide-by-zero.
	 */
	private String computeAccuracy(List<QuizResult> results) {
		int totalQuestions = results.stream()
				.map(QuizResult::getTotalQuestions)
				.filter(java.util.Objects::nonNull)
				.mapToInt(Integer::intValue)
				.sum();

		if (totalQuestions == 0) {
			return "—";
		}

		int totalScore = results.stream()
				.map(QuizResult::getScore)
				.filter(java.util.Objects::nonNull)
				.mapToInt(Integer::intValue)
				.sum();

		double accuracy = (totalScore * 100.0) / totalQuestions;
		return String.format(Locale.ROOT, "%.1f%%", accuracy);
	}
}
