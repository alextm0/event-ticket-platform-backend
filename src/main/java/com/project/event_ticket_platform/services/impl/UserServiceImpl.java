package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.CreateUserRequest;
import com.project.event_ticket_platform.dtos.UserResponse;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.entities.UserRole;
import com.project.event_ticket_platform.exceptions.EmailAlreadyExistsException;
import com.project.event_ticket_platform.exceptions.UserNotFoundException;
import com.project.event_ticket_platform.repositories.UserRepository;
import com.project.event_ticket_platform.services.UserService;
import com.project.event_ticket_platform.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;

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
		return userMapper.toResponse(savedUser);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getUserById(UUID userId) {
		return userRepository.findById(userId)
				.map(userMapper::toResponse)
				.orElseThrow(() -> new UserNotFoundException(userId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll()
				.stream()
				.map(userMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponse> getStaffUsers() {
		return userRepository.findByRole(UserRole.STAFF)
				.stream()
				.map(userMapper::toResponse)
				.toList();
	}

}
