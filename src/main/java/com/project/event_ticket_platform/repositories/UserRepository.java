package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	boolean existsByEmail(String email);

	java.util.Optional<User> findByEmail(String email);
}
