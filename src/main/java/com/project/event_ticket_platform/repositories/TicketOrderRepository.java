package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.TicketOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TicketOrderRepository extends JpaRepository<TicketOrder, UUID> {

    @Query("SELECT DISTINCT o FROM TicketOrder o " +
            "JOIN o.tickets t " +
            "JOIN t.ticketType tt " +
            "WHERE tt.event.id = :eventId " +
            "ORDER BY o.createdAt DESC")
    Page<TicketOrder> findByEventId(@Param("eventId") UUID eventId, Pageable pageable);
}
