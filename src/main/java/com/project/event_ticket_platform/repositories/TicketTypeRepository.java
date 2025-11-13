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

	/**
	 * Fetches the TicketType with its associated Event by id while acquiring a pessimistic write lock.
	 *
	 * @param id the UUID of the TicketType to retrieve
	 * @return an Optional containing the TicketType with its Event if found, or an empty Optional if not found
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT tt FROM TicketType tt JOIN FETCH tt.event WHERE tt.id = :id")
	Optional<TicketType> findByIdWithLock(@Param("id") UUID id);

	/**
	 * Retrieves ticket types associated with a specific event as a paginated list.
	 *
	 * @param eventId the UUID of the event whose ticket types should be returned
	 * @param pageable pagination and sorting information to apply to the result
	 * @return a page of TicketType entities linked to the given event, or an empty page if none are found
	 */
	@Query("SELECT tt FROM TicketType tt WHERE tt.event.id = :eventId")
	Page<TicketType> findByEventId(UUID eventId, Pageable pageable);
}
