package com.oopstudio.lms.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.oopstudio.lms.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
				.map(user -> org.springframework.security.core.userdetails.User
						.withUsername(user.getUsername())
						.password(user.getPassword())
						.roles(user.getRole().name())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("No user account found for username: " + username));
	}
}
