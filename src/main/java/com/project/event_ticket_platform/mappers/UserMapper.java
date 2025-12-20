package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.UserResponse;
import com.project.event_ticket_platform.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
