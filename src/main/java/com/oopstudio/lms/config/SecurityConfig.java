package com.oopstudio.lms.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/", "/css/**", "/js/**", "/login", "/register", "/error").permitAll()
						.requestMatchers("/api/public/**").permitAll()
						.requestMatchers("/instructor/**", "/api/instructor/**").hasRole("TEACHER")
						.anyRequest().authenticated()
				)
				.formLogin(form -> form
						.loginPage("/login")
						.successHandler(roleAwareAuthenticationSuccessHandler())
						.permitAll()
				)
				.logout(logout -> logout
						.logoutRequestMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/logout"))
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("JSESSIONID")
						.logoutSuccessUrl("/login?logout")
						.permitAll()
				)
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationSuccessHandler roleAwareAuthenticationSuccessHandler() {
		return new RoleAwareAuthenticationSuccessHandler();
	}

	private static final class RoleAwareAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

		@Override
		public void onAuthenticationSuccess(
				HttpServletRequest request,
				HttpServletResponse response,
				Authentication authentication
		) throws IOException, ServletException {
			boolean isTeacher = authentication.getAuthorities().stream()
					.map(GrantedAuthority::getAuthority)
					.anyMatch("ROLE_TEACHER"::equals);

			String targetPath = isTeacher ? "/instructor/dashboard" : "/practice";
			response.sendRedirect(request.getContextPath() + targetPath);
		}
	}
}
