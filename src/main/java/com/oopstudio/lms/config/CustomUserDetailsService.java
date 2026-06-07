package com.oopstudio.lms.config;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.oopstudio.lms.models.User;
import com.oopstudio.lms.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String loginIdentifier) throws UsernameNotFoundException {
		String normalizedIdentifier = normalizeLoginIdentifier(loginIdentifier);
		return findUserByLoginIdentifier(normalizedIdentifier)
				.map(user -> org.springframework.security.core.userdetails.User
						.withUsername(resolvePrincipalName(user))
						.password(user.getPassword())
						.roles(user.getRole().name())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("No user account found for login identifier."));
	}

	private java.util.Optional<User> findUserByLoginIdentifier(String loginIdentifier) {
		if (loginIdentifier.contains("@")) {
			return userRepository.findByEmail(loginIdentifier.toLowerCase(Locale.ROOT));
		}

		return userRepository.findByUniqueTeacherId(loginIdentifier.toUpperCase(Locale.ROOT));
	}

	private String resolvePrincipalName(User user) {
		if (user.getRole() == User.Role.TEACHER) {
			return user.getUniqueTeacherId();
		}

		return user.getEmail();
	}

	private String normalizeLoginIdentifier(String loginIdentifier) {
		if (loginIdentifier == null || loginIdentifier.isBlank()) {
			throw new UsernameNotFoundException("Login identifier is required.");
		}

		return loginIdentifier.trim();
	}
}
