package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

	@Query("SELECT DISTINCT e FROM Event e " +
		"LEFT JOIN FETCH e.ticketTypes tt " +
		"WHERE e.status = :status")
	List<Event> findAllByStatus(@Param("status") EventStatus status);

	@Query("SELECT e FROM Event e " +
		"LEFT JOIN FETCH e.ticketTypes tt " +
		"WHERE e.id = :id")
	Optional<Event> findByIdWithTicketTypes(@Param("id") UUID id);
}
