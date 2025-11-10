package com.project.event_ticket_platform.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Ticket sale information for an event")
public record EventTicketSaleResponse(
    UUID id,
    UUID eventId,
    UUID ticketTypeId,
    String ticketTypeName,
    UUID buyerId,
    String buyerName,
    Integer quantity,
    Instant purchaseDate
){}
