package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	@Query(value = "SELECT * FROM users WHERE role = CAST(:role AS user_role)", nativeQuery = true)
	List<User> findByRole(@Param("role") String role);
}
