package com.project.event_ticket_platform.repository;

import com.project.event_ticket_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	boolean existsByEmail(String email);
}
