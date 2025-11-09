package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

	@Query("SELECT t FROM Ticket t " +
		   "JOIN FETCH t.ticketType tt " +
		   "JOIN FETCH tt.event e " +
		   "JOIN FETCH t.order o " +
		   "WHERE o.user.id = :userId")
	List<Ticket> findAllByUserId(@Param("userId") UUID userId);
}

