package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.dtos.EventTicketSaleResponse;
import com.project.event_ticket_platform.dtos.UpdateEventRequest;
import com.project.event_ticket_platform.services.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
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
}
