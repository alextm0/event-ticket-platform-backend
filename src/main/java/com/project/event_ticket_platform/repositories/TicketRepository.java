package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
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

	@Query("SELECT t FROM Ticket t " +
		   "JOIN FETCH t.ticketType tt " +
		   "JOIN FETCH tt.event e " +
		   "JOIN FETCH t.qrCode qr " +
		   "JOIN FETCH t.order o " +
		   "WHERE t.order.id = :orderId")
	List<Ticket> findByOrderIdWithRelations(@Param("orderId") UUID orderId);

	@Query("SELECT t FROM Ticket t " +
		   "JOIN FETCH t.qrCode qr " +
		   "JOIN FETCH t.order o " +
		   "JOIN FETCH o.user u " +
		   "WHERE t.id = :ticketId")
	java.util.Optional<Ticket> findByIdWithQrCode(@Param("ticketId") UUID ticketId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT t FROM Ticket t " +
		   "JOIN FETCH t.ticketType tt " +
		   "JOIN FETCH tt.event e " +
		   "JOIN FETCH t.qrCode qr " +
		   "WHERE qr.id = :qrCodeId")
	java.util.Optional<Ticket> findByQrCodeIdWithEventForUpdate(@Param("qrCodeId") UUID qrCodeId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT t FROM Ticket t " +
		   "JOIN FETCH t.ticketType tt " +
		   "JOIN FETCH tt.event e " +
		   "JOIN FETCH t.qrCode qr " +
		   "WHERE t.id = :ticketId")
	java.util.Optional<Ticket> findByTicketIdWithEventForUpdate(@Param("ticketId") UUID ticketId);

	@Query("SELECT t FROM Ticket t " +
		   "JOIN t.ticketType tt " +
		   "JOIN tt.event e " +
			"JOIN t.order o " +
		   "WHERE e.id = :eventId")
	Page<Ticket> findAllByEventId(@Param("eventId") UUID eventId, Pageable pageable);
}

