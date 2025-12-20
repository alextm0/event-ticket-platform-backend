package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.CreateUserRequest;
import com.project.event_ticket_platform.dtos.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

	UserResponse createUser(CreateUserRequest request);

	UserResponse getUserById(UUID userId);

	List<UserResponse> getAllUsers();

	List<UserResponse> getStaffUsers();
}
