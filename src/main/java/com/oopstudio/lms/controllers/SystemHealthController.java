package com.oopstudio.lms.controllers;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemHealthController {

	@GetMapping(value = {"/", "/api/health"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> healthCheck() {
		return Map.of(
				"status", "UP",
				"message", "OOP Studio LMS Backend Engine is securely compiled and running on Java 25!"
		);
	}
}
