package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.CreateUserRequest;
import com.project.event_ticket_platform.dtos.UserResponse;
import com.project.event_ticket_platform.entities.UserRole;
import com.project.event_ticket_platform.exceptions.EmailAlreadyExistsException;
import com.project.event_ticket_platform.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "app.jpa.auditing.enabled=false")
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private com.project.event_ticket_platform.services.JwtService jwtService;

	@Test
	void shouldCreateUser() throws Exception {
		UUID generatedId = UUID.randomUUID();
		UserResponse response = new UserResponse(
				generatedId,
				"Jane Doe",
				"jane.doe@example.com",
				UserRole.ATTENDEE,
				Instant.now(),
				Instant.now());

		when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
							"name": "Jane Doe",
							"email": "jane.doe@example.com",
							"password": "password123",
							"role": "ATTENDEE"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/users/" + generatedId))
				.andExpect(jsonPath("$.id").value(generatedId.toString()))
				.andExpect(jsonPath("$.name").value("Jane Doe"))
				.andExpect(jsonPath("$.email").value("jane.doe@example.com"))
				.andExpect(jsonPath("$.role").value("ATTENDEE"));

		ArgumentCaptor<CreateUserRequest> captor = ArgumentCaptor.forClass(CreateUserRequest.class);
		verify(userService).createUser(captor.capture());
		CreateUserRequest capturedRequest = captor.getValue();
		assertThat(capturedRequest.name()).isEqualTo("Jane Doe");
		assertThat(capturedRequest.email()).isEqualTo("jane.doe@example.com");
		assertThat(capturedRequest.password()).isEqualTo("password123");
	}

	@Test
	void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
		when(userService.createUser(any(CreateUserRequest.class)))
				.thenThrow(new EmailAlreadyExistsException("jane.doe@example.com"));

		mockMvc.perform(post("/api/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
							"name": "Jane Doe",
							"email": "jane.doe@example.com",
							"password": "password123"
						}
						"""))
				.andExpect(status().isConflict());
	}

	@Test
	void shouldReturnCurrentUser() throws Exception {
		UUID userId = UUID.randomUUID();
		UserResponse response = new UserResponse(
				userId,
				"Authenticated User",
				"auth@example.com",
				UserRole.ATTENDEE,
				Instant.now(),
				Instant.now());

		when(userService.getUserById(userId)).thenReturn(response);

		mockMvc.perform(get("/api/v1/users/me")
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId.toString()))
				.andExpect(jsonPath("$.name").value("Authenticated User"));
	}

	@Test
	void shouldReturnUserById() throws Exception {
		UUID userId = UUID.randomUUID();
		UserResponse response = new UserResponse(
				userId,
				"Specific User",
				"specific@example.com",
				UserRole.STAFF,
				Instant.now(),
				Instant.now());

		when(userService.getUserById(userId)).thenReturn(response);

		mockMvc.perform(get("/api/v1/users/" + userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId.toString()))
				.andExpect(jsonPath("$.name").value("Specific User"));
	}

	@Test
	void shouldListUsers() throws Exception {
		UserResponse response = new UserResponse(
				UUID.randomUUID(),
				"John Doe",
				"john.doe@example.com",
				UserRole.STAFF,
				Instant.now(),
				Instant.now());

		when(userService.getAllUsers()).thenReturn(List.of(response));

		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("John Doe"))
				.andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
				.andExpect(jsonPath("$[0].role").value("STAFF"));
	}

}
