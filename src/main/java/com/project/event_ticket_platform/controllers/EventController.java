package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.*;
import com.project.event_ticket_platform.services.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "APIs for managing events and related ticket types and sales")
public class EventController {

	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@PostMapping
	public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
		EventResponse createdEvent = eventService.createEvent(request);
		return ResponseEntity
			.created(URI.create("/api/v1/events/" + createdEvent.id()))
			.body(createdEvent);
	}

	@GetMapping
	public Page<EventResponse> getEvents(Pageable pageable) {
		return eventService.getAllEvents(pageable);
	}

	@PutMapping("/{eventId}")
	public EventResponse updateEvent(
		@PathVariable UUID eventId,
		@Valid @RequestBody UpdateEventRequest request
	) {
		return eventService.updateEvent(eventId, request);
	}

	@DeleteMapping("/{eventId}")
	public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
		eventService.deleteEvent(eventId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{eventId}/tickets")
	public Page<EventTicketSaleResponse> getTicketSalesForEvent(@PathVariable UUID eventId, Pageable pageable) {
		return eventService.getTicketSalesForEvent(eventId, pageable);
	}

	@GetMapping("/{eventId}/tickets/{ticketId}")
	public EventTicketSaleResponse getTicketSale(@PathVariable UUID eventId, @PathVariable UUID ticketId) {
		return eventService.getTicketSaleForEvent(eventId, ticketId);
	}

	@GetMapping("/{eventId}/ticket-types")
	public Page<TicketTypeResponse> getTicketTypesForEvent(@PathVariable UUID eventId, Pageable pageable) {
		return eventService.getTicketTypesForEvent(eventId, pageable);
	}

	@GetMapping("/{eventId}/ticket-types/{ticketTypeId}")
	public TicketTypeResponse getTicketTypeForEvent(@PathVariable UUID eventId, @PathVariable UUID ticketTypeId) {
		return eventService.getTicketTypeForEvent(eventId, ticketTypeId);
	}

	@DeleteMapping("/{eventId}/ticket-types/{ticketTypeId}")
	public ResponseEntity<Void> deleteTicketTypeFromEvent(@PathVariable UUID eventId, @PathVariable UUID ticketTypeId) {
		eventService.deleteTicketTypeForEvent(eventId, ticketTypeId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{eventId}/ticket-types/{ticketTypeId}")
	public TicketTypeResponse patchTicketTypeForEvent(
			@PathVariable UUID eventId,
			@PathVariable UUID ticketTypeId,
			@RequestBody PatchTicketTypeRequest request
	) {
		return eventService.patchTicketTypeForEvent(eventId, ticketTypeId, request);
	}
}
