package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {
}

