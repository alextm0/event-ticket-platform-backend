package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.TicketValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketValidationRepository extends JpaRepository<TicketValidation, UUID> {

	@Query("SELECT tv FROM TicketValidation tv " +
			"LEFT JOIN FETCH tv.ticket t " +
			"LEFT JOIN FETCH t.ticketType tt " +
			"LEFT JOIN FETCH tt.event " +
			"LEFT JOIN FETCH t.qrCode qr " +
			"JOIN FETCH tv.event e " +
			"WHERE e.id = :eventId " +
			"ORDER BY tv.validationDateTime DESC")
	List<TicketValidation> findAllByEventIdWithDetails(@Param("eventId") UUID eventId);
}
