package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.CreateUserRequest;
import com.project.event_ticket_platform.dtos.LoginRequest;
import com.project.event_ticket_platform.dtos.LoginResponse;
import com.project.event_ticket_platform.dtos.UserResponse;
import com.project.event_ticket_platform.services.AuthService;
import com.project.event_ticket_platform.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final UserService userService;

	@Operation(summary = "User login", description = "Authenticate a user with email and password. Returns a JWT token on success.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request or credentials", content = @Content),
			@ApiResponse(responseCode = "401", description = "Invalid email, password, or role", content = @Content)
	})
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Check signup status", description = "Returns whether user signup is enabled")
	@ApiResponse(responseCode = "200", description = "Signup status retrieved", content = @Content(schema = @Schema(implementation = Map.class)))
	@GetMapping("/signupStatus")
	public ResponseEntity<Map<String, Boolean>> signupStatus() {
		return ResponseEntity.ok(Map.of("enabled", true));
	}

	@Operation(summary = "User signup", description = "Register a new user account")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
			@ApiResponse(responseCode = "409", description = "Email already exists", content = @Content)
	})
	@PostMapping("/signup")
	public ResponseEntity<UserResponse> signup(@Valid @RequestBody CreateUserRequest request) {
		UserResponse createdUser = userService.createUser(request);
		return ResponseEntity
				.created(URI.create("/api/v1/auth/signup/" + createdUser.id()))
				.body(createdUser);
	}
}
