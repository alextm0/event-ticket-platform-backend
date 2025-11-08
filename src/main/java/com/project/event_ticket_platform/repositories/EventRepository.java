package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

	@Query(value = "SELECT * FROM events WHERE status = CAST(:status AS event_status)", nativeQuery = true)
	List<Event> findAllByStatus(@Param("status") String status);
}
