package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

	@Query("SELECT t FROM Ticket t " +
		"JOIN FETCH t.order o " +
		"JOIN FETCH t.ticketType tt " +
		"JOIN FETCH tt.event " +
		"JOIN FETCH t.qrCode " +
		"WHERE o.user.id = :userId")
	List<Ticket> findAllByUserId(@Param("userId") UUID userId);

	@Query("SELECT t FROM Ticket t " +
		"JOIN FETCH t.order o " +
		"JOIN FETCH o.user " +
		"JOIN FETCH t.ticketType tt " +
		"JOIN FETCH tt.event " +
		"JOIN FETCH t.qrCode " +
		"WHERE t.id = :id")
	Optional<Ticket> findByIdWithRelations(@Param("id") UUID id);
}

