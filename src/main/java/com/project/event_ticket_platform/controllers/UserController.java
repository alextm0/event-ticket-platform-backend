package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.CreateUserRequest;
import com.project.event_ticket_platform.dtos.UserResponse;
import com.project.event_ticket_platform.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		UserResponse createdUser = userService.createUser(request);
		return ResponseEntity
			.created(URI.create("/api/users/" + createdUser.id()))
			.body(createdUser);
	}

	@GetMapping
	public List<UserResponse> getUsers() {
		return userService.getAllUsers();
	}
}
