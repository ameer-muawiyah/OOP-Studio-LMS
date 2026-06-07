package com.oopstudio.lms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

	@GetMapping("/practice")
	public String practice() {
		return "practice";
	}

	@GetMapping("/code-practice")
	public String codePractice() {
		return "code-practice";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}
}
