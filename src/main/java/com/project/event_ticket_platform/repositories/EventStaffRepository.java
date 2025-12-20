package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.EventStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventStaffRepository extends JpaRepository<EventStaff, UUID> {

	boolean existsByEventIdAndStaffId(UUID eventId, UUID staffId);

	@Query("SELECT es.event.id FROM EventStaff es WHERE es.staff.id = :staffId")
	List<UUID> findEventIdsByStaffId(@Param("staffId") UUID staffId);

	@Query("SELECT es FROM EventStaff es JOIN FETCH es.event WHERE es.staff.id = :staffId")
	List<EventStaff> findByStaffIdWithEvent(@Param("staffId") UUID staffId);

	void deleteByEventIdAndStaffId(UUID eventId, UUID staffId);

	@Query("SELECT es FROM EventStaff es JOIN FETCH es.staff WHERE es.event.id = :eventId")
	List<EventStaff> findByEventIdWithStaff(@Param("eventId") UUID eventId);
}
