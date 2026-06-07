package com.oopstudio.lms.controllers;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oopstudio.lms.models.User;
import com.oopstudio.lms.repositories.UserRepository;

@RestController
public class RegistrationController {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public RegistrationController(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/api/public/teachers")
	public List<TeacherDirectoryResponse> teachers() {
		return userRepository.findByRoleOrderByLastNameAscFirstNameAsc(User.Role.TEACHER).stream()
				.map(teacher -> new TeacherDirectoryResponse(
						teacher.getId(),
						teacher.getFirstName(),
						teacher.getLastName()
				))
				.toList();
	}

	@PostMapping("/api/public/register/teacher")
	public ResponseEntity<?> registerTeacher(
			@RequestParam String firstName,
			@RequestParam String lastName,
			@RequestParam String email,
			@RequestParam String password
	) {
		String normalizedFirstName = normalizeName(firstName, "firstName");
		String normalizedLastName = normalizeName(lastName, "lastName");
		String normalizedEmail = normalizeEmail(email);
		String normalizedPassword = normalizePassword(password);

		if (userRepository.existsByEmail(normalizedEmail)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
					"DUPLICATE_EMAIL",
					"An account already exists for this email address."
			));
		}

		String teacherId = generateUniqueTeacherId();

		User teacher = new User();
		teacher.setFirstName(normalizedFirstName);
		teacher.setLastName(normalizedLastName);
		teacher.setEmail(normalizedEmail);
		teacher.setUniqueTeacherId(teacherId);
		teacher.setUsername(teacherId);
		teacher.setPassword(passwordEncoder.encode(normalizedPassword));
		teacher.setRole(User.Role.TEACHER);

		User savedTeacher = userRepository.save(teacher);
		return ResponseEntity.status(HttpStatus.CREATED).body(new TeacherRegistrationResponse(
				savedTeacher.getId(),
				savedTeacher.getUniqueTeacherId(),
				"Teacher account created. Store the generated ID securely. The custom security password is active."
		));
	}

	@PostMapping("/api/public/register/student")
	public ResponseEntity<?> registerStudent(
			@RequestParam String firstName,
			@RequestParam String lastName,
			@RequestParam String email,
			@RequestParam String password,
			@RequestParam Long supervisorId
	) {
		String normalizedFirstName = normalizeName(firstName, "firstName");
		String normalizedLastName = normalizeName(lastName, "lastName");
		String normalizedEmail = normalizeEmail(email);
		String normalizedPassword = normalizePassword(password);

		if (userRepository.existsByEmail(normalizedEmail)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
					"DUPLICATE_EMAIL",
					"An account already exists for this email address."
			));
		}

		User supervisor = userRepository.findById(supervisorId).orElse(null);
		if (supervisor == null || supervisor.getRole() != User.Role.TEACHER) {
			return ResponseEntity.badRequest().body(new ErrorResponse(
					"INVALID_SUPERVISOR",
					"Select a valid instructor supervisor before creating a student account."
			));
		}

		User student = new User();
		student.setFirstName(normalizedFirstName);
		student.setLastName(normalizedLastName);
		student.setEmail(normalizedEmail);
		student.setUsername(normalizedEmail);
		student.setPassword(passwordEncoder.encode(normalizedPassword));
		student.setRole(User.Role.STUDENT);
		student.setSupervisor(supervisor);

		User savedStudent = userRepository.save(student);
		return ResponseEntity.status(HttpStatus.CREATED).body(new StudentRegistrationResponse(
				savedStudent.getId(),
				savedStudent.getEmail(),
				supervisor.getId(),
				"Student account created. Use the email address and password to sign in."
		));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException exception) {
		return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", exception.getMessage()));
	}

	private String generateUniqueTeacherId() {
		for (int attempt = 0; attempt < 100; attempt++) {
			String candidate = "T-" + (1000 + SECURE_RANDOM.nextInt(9000));
			if (!userRepository.existsByUniqueTeacherId(candidate)) {
				return candidate;
			}
		}

		throw new IllegalStateException("Unable to generate a unique teacher ID.");
	}

	private String normalizeName(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}

		String normalized = value.trim();
		if (normalized.length() > 80) {
			throw new IllegalArgumentException(fieldName + " must be 80 characters or fewer.");
		}

		return normalized;
	}

	private String normalizeEmail(String email) {
		if (email == null || email.isBlank() || !email.contains("@")) {
			throw new IllegalArgumentException("A valid email address is required.");
		}

		String normalized = email.trim().toLowerCase(Locale.ROOT);
		if (normalized.length() > 160) {
			throw new IllegalArgumentException("Email must be 160 characters or fewer.");
		}

		return normalized;
	}

	private String normalizePassword(String password) {
		if (password == null || password.length() < 8) {
			throw new IllegalArgumentException("Password must contain at least 8 characters.");
		}

		return password;
	}

	public record TeacherDirectoryResponse(Long id, String firstName, String lastName) {
	}

	public record TeacherRegistrationResponse(
			Long id,
			String uniqueTeacherId,
			String message
	) {
	}

	public record StudentRegistrationResponse(
			Long id,
			String email,
			Long supervisorId,
			String message
	) {
	}

	public record ErrorResponse(String code, String message) {
	}
}
