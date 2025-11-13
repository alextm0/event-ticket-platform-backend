package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.*;
import com.project.event_ticket_platform.services.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

	/**
	 * Deletes the event identified by the given UUID.
	 *
	 * @param eventId the UUID of the event to delete
	 * @return a ResponseEntity with HTTP 204 No Content when the event is deleted
	 */
	@DeleteMapping("/{eventId}")
	public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
		eventService.deleteEvent(eventId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Retrieve ticket sales for the specified event.
	 *
	 * @param eventId the UUID of the event whose ticket sales are requested
	 * @param pageable pagination and sorting parameters for the returned page
	 * @return a page of EventTicketSaleResponse objects representing ticket sales for the event
	 */
	@GetMapping("/{eventId}/tickets")
	public Page<EventTicketSaleResponse> getTicketSalesForEvent(@PathVariable UUID eventId, Pageable pageable) {
		return eventService.getTicketSalesForEvent(eventId, pageable);
	}

	/**
	 * Retrieves a single ticket sale associated with the specified event.
	 *
	 * @param eventId the UUID of the event
	 * @param ticketId the UUID of the ticket sale
	 * @return the ticket sale as an EventTicketSaleResponse
	 */
	@GetMapping("/{eventId}/tickets/{ticketId}")
	public EventTicketSaleResponse getTicketSale(@PathVariable UUID eventId, @PathVariable UUID ticketId) {
		return eventService.getTicketSaleForEvent(eventId, ticketId);
	}

	/**
	 * Retrieve ticket types available for a specific event.
	 *
	 * @param eventId the UUID of the event whose ticket types are being requested
	 * @param pageable pagination and sorting parameters for the returned page
	 * @return a page of TicketTypeResponse objects representing the event's ticket types
	 */
	@GetMapping("/{eventId}/ticket-types")
	public Page<TicketTypeResponse> getTicketTypesForEvent(@PathVariable UUID eventId, Pageable pageable) {
		return eventService.getTicketTypesForEvent(eventId, pageable);
	}

	/**
	 * Retrieve the ticket type for a specific event.
	 *
	 * @param eventId      the UUID of the event
	 * @param ticketTypeId the UUID of the ticket type
	 * @return the TicketTypeResponse for the specified ticket type of the event
	 */
	@GetMapping("/{eventId}/ticket-types/{ticketTypeId}")
	public TicketTypeResponse getTicketTypeForEvent(@PathVariable UUID eventId, @PathVariable UUID ticketTypeId) {
		return eventService.getTicketTypeForEvent(eventId, ticketTypeId);
	}

	/**
	 * Deletes the ticket type with the given ID from the specified event.
	 *
	 * @param eventId the ID of the event
	 * @param ticketTypeId the ID of the ticket type to remove from the event
	 * @return a response with HTTP status 204 No Content when deletion succeeds
	 */
	@DeleteMapping("/{eventId}/ticket-types/{ticketTypeId}")
	public ResponseEntity<Void> deleteTicketTypeFromEvent(@PathVariable UUID eventId, @PathVariable UUID ticketTypeId) {
		eventService.deleteTicketTypeForEvent(eventId, ticketTypeId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Applies partial updates to a ticket type for the specified event.
	 *
	 * @param request the patch payload containing fields to update on the ticket type; unspecified fields are left unchanged
	 * @return the updated TicketTypeResponse reflecting the persisted changes
	 */
	@PatchMapping("/{eventId}/ticket-types/{ticketTypeId}")
	public TicketTypeResponse patchTicketTypeForEvent(
			@PathVariable UUID eventId,
			@PathVariable UUID ticketTypeId,
			@RequestBody PatchTicketTypeRequest request
	) {
		return eventService.patchTicketTypeForEvent(eventId, ticketTypeId, request);
	}
}