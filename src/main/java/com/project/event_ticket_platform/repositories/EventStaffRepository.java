package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.EventStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventStaffRepository extends JpaRepository<EventStaff, UUID> {

	boolean existsByEventIdAndStaffId(UUID eventId, UUID staffId);
}
