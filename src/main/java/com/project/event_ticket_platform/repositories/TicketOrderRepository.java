package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.TicketOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketOrderRepository extends JpaRepository<TicketOrder, UUID> {
}

