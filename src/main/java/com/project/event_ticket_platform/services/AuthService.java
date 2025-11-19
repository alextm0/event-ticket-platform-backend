package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.LoginRequest;
import com.project.event_ticket_platform.dtos.LoginResponse;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.exceptions.InvalidCredentialsException;
import com.project.event_ticket_platform.exceptions.InvalidRoleException;
import com.project.event_ticket_platform.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtService jwtService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

		// Verify password first
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException("Invalid email or password");
		}

		// Verify role matches (email and password are correct at this point)
		if (request.role() != null && user.getRole() != request.role()) {
			throw new InvalidRoleException("Invalid role for this user");
		}

		// Generate JWT token
		String token = jwtService.generateToken(
			user.getId(),
			user.getEmail(),
			user.getRole().name()
		);

		return new LoginResponse(
			user.getId(),
			user.getEmail(),
			user.getRole(),
			token
		);
	}
}
