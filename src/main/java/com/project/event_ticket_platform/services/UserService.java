package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.CreateUserRequest;
import com.project.event_ticket_platform.dtos.UserResponse;

import java.util.List;

public interface UserService {

	UserResponse createUser(CreateUserRequest request);

	List<UserResponse> getAllUsers();
}
