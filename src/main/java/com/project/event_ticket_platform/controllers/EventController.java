package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.*;
import com.project.event_ticket_platform.exceptions.UnauthorizedEventAccessException;
import com.project.event_ticket_platform.services.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import org.springdoc.core.annotations.ParameterObject;

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
	@Parameters({
			@Parameter(name = "page", description = "Zero-based page index", example = "0"),
			@Parameter(name = "size", description = "Page size", example = "20"),
			@Parameter(name = "sort", description = "Sorting criteria in the format property,(asc|desc). Example: startTime,asc", example = "startTime,asc"),
			@Parameter(name = "organizerId", description = "Filter events by organizer ID", example = "517e1133-7615-4c73-8634-728d64c0511f")
	})
	public Page<EventResponse> getEvents(
			@RequestParam(value = "organizerId", required = false) UUID organizerId,
			@ParameterObject Pageable pageable) {
		if (organizerId != null) {
			return eventService.getEventsByOrganizer(organizerId, pageable);
		}
		return eventService.getAllEvents(pageable);
	}

	@PutMapping("/{eventId}")
	@PreAuthorize("@eventSecurityService.canModifyEvent(#eventId, authentication)")
	public EventResponse updateEvent(
			@PathVariable UUID eventId,
			@Valid @RequestBody UpdateEventRequest request) {
		return eventService.updateEvent(eventId, request);
	}

	@DeleteMapping("/{eventId}")
	@PreAuthorize("@eventSecurityService.canDeleteEvent(#eventId, authentication)")
	public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
		eventService.deleteEvent(eventId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{eventId}/tickets")
	@Parameters({
			@Parameter(name = "page", description = "Zero-based page index", example = "0"),
			@Parameter(name = "size", description = "Page size", example = "20"),
			@Parameter(name = "sort", description = "Sorting criteria in the format property,(asc|desc). Example: createdAt,desc", example = "createdAt,desc")
	})
	public Page<EventTicketSaleResponse> getTicketSalesForEvent(@PathVariable UUID eventId,
			@ParameterObject Pageable pageable) {
		return eventService.getTicketSalesForEvent(eventId, pageable);
	}

	@GetMapping("/{eventId}/tickets/{ticketId}")
	public EventTicketSaleResponse getTicketSale(@PathVariable UUID eventId, @PathVariable UUID ticketId) {
		return eventService.getTicketSaleForEvent(eventId, ticketId);
	}

	@GetMapping("/{eventId}/ticket-types")
	@Parameters({
			@Parameter(name = "page", description = "Zero-based page index", example = "0"),
			@Parameter(name = "size", description = "Page size", example = "20"),
			@Parameter(name = "sort", description = "Sorting criteria in the format property,(asc|desc). Example: name,asc", example = "name,asc")
	})
	public Page<TicketTypeResponse> getTicketTypesForEvent(@PathVariable UUID eventId,
			@ParameterObject Pageable pageable) {
		return eventService.getTicketTypesForEvent(eventId, pageable);
	}

	@GetMapping("/{eventId}/ticket-types/{ticketTypeId}")
	public TicketTypeResponse getTicketTypeForEvent(@PathVariable UUID eventId, @PathVariable UUID ticketTypeId) {
		return eventService.getTicketTypeForEvent(eventId, ticketTypeId);
	}

	@PostMapping("/{eventId}/ticket-types")
	@PreAuthorize("@eventSecurityService.canModifyEvent(#eventId, authentication)")
	public ResponseEntity<TicketTypeResponse> createTicketTypeForEvent(
			@PathVariable UUID eventId,
			@Valid @RequestBody CreateTicketTypeRequest request) {
		TicketTypeResponse createdTicketType = eventService.createTicketTypeForEvent(eventId, request);
		return ResponseEntity
				.created(URI.create("/api/v1/events/" + eventId + "/ticket-types/" + createdTicketType.id()))
				.body(createdTicketType);
	}

	@DeleteMapping("/{eventId}/ticket-types/{ticketTypeId}")
	@PreAuthorize("@eventSecurityService.canModifyEvent(#eventId, authentication)")
	public ResponseEntity<Void> deleteTicketTypeFromEvent(@PathVariable UUID eventId, @PathVariable UUID ticketTypeId) {
		eventService.deleteTicketTypeForEvent(eventId, ticketTypeId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{eventId}/ticket-types/{ticketTypeId}")
	@PreAuthorize("@eventSecurityService.canModifyEvent(#eventId, authentication)")
	public TicketTypeResponse patchTicketTypeForEvent(
			@PathVariable UUID eventId,
			@PathVariable UUID ticketTypeId,
			@RequestBody PatchTicketTypeRequest request) {
		return eventService.patchTicketTypeForEvent(eventId, ticketTypeId, request);
	}
}
