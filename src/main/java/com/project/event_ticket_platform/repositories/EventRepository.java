package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
