package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.CreateUserRequest;
import com.project.event_ticket_platform.dtos.UserResponse;
import com.project.event_ticket_platform.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		UserResponse createdUser = userService.createUser(request);
		return ResponseEntity
				.created(URI.create("/api/v1/users/" + createdUser.id()))
				.body(createdUser);
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> getCurrentUser(
			@RequestHeader("X-User-Id") UUID userId) {
		return ResponseEntity.ok(userService.getUserById(userId));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
		return ResponseEntity.ok(userService.getUserById(userId));
	}

	@GetMapping
	public List<UserResponse> getUsers() {
		return userService.getAllUsers();
	}

	@GetMapping("/staff")
	public List<UserResponse> getStaff() {
		return userService.getStaffUsers();
	}
}
