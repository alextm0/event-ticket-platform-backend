package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

	@Query("SELECT tt FROM TicketType tt JOIN FETCH tt.event WHERE tt.id = :id")
	Optional<TicketType> findByIdWithEvent(@Param("id") UUID id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT tt FROM TicketType tt JOIN FETCH tt.event WHERE tt.id = :id")
	Optional<TicketType> findByIdWithLock(@Param("id") UUID id);

	@Query("SELECT tt FROM TicketType tt WHERE tt.event.id = :eventId")
	Page<TicketType> findByEventId(@Param("eventId") UUID eventId, Pageable pageable);
}

