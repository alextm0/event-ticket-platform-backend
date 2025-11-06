package com.project.event_ticket_platform.service;

import com.project.event_ticket_platform.dto.CreateUserRequest;
import com.project.event_ticket_platform.dto.UserResponse;
import com.project.event_ticket_platform.entity.User;
import com.project.event_ticket_platform.entity.UserRole;
import com.project.event_ticket_platform.exception.EmailAlreadyExistsException;
import com.project.event_ticket_platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional
	public UserResponse createUser(CreateUserRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new EmailAlreadyExistsException(request.email());
		}

		User user = new User();
		user.setId(UUID.randomUUID());
		user.setName(request.name());
		user.setEmail(request.email());
		user.setPasswordHash(request.password());
		user.setRole(request.role() != null ? request.role() : UserRole.ATTENDEE);

		User savedUser = userRepository.save(user);
		return toResponse(savedUser);
	}

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
