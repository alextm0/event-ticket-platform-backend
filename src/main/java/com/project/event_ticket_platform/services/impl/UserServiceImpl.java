package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.CreateUserRequest;
import com.project.event_ticket_platform.dtos.UserResponse;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.entities.UserRole;
import com.project.event_ticket_platform.exceptions.EmailAlreadyExistsException;
import com.project.event_ticket_platform.repositories.UserRepository;
import com.project.event_ticket_platform.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public UserResponse createUser(CreateUserRequest request) {
		String name = request.name();
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Name cannot be null or blank");
		}

		String email = request.email();
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email cannot be null or blank");
		}

		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyExistsException(email);
		}

		String rawPassword = request.password();
		if (rawPassword == null || rawPassword.isBlank()) {
			throw new IllegalArgumentException("Password cannot be null or blank");
		}

		User user = new User();
		user.setId(UUID.randomUUID());
		user.setName(name);
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		user.setRole(request.role() != null ? request.role() : UserRole.ATTENDEE);

		User savedUser = userRepository.save(user);
		return toResponse(savedUser);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll()
			.stream()
			.map(this::toResponse)
			.toList();
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
			user.getId(),
			user.getName(),
			user.getEmail(),
			user.getRole(),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}

