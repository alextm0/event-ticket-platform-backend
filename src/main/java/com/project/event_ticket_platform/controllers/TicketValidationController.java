package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.TicketValidationResponse;
import com.project.event_ticket_platform.dtos.ValidateTicketRequest;
import com.project.event_ticket_platform.services.TicketValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events/{event_id}/ticket-validations")
@Tag(name = "Ticket Validations", description = "Staff endpoints to validate tickets")
public class TicketValidationController {

	private final TicketValidationService ticketValidationService;

	public TicketValidationController(TicketValidationService ticketValidationService) {
		this.ticketValidationService = ticketValidationService;
	}

	@Operation(
		summary = "Validate a ticket",
		description = "Scan a QR code to validate a ticket for the given event. Only staff members assigned to the event may call this endpoint.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Ticket validated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid payload"),
			@ApiResponse(responseCode = "403", description = "User is not staff or not assigned to the event"),
			@ApiResponse(responseCode = "404", description = "Event or ticket not found")
		}
	)
	@PostMapping
	public ResponseEntity<TicketValidationResponse> validateTicket(
		@Parameter(description = "Event identifier", required = true)
		@PathVariable("event_id") UUID eventId,
		@Parameter(description = "Staff user identifier", required = true)
		@RequestHeader("X-User-Id") UUID staffId,
		@Valid @RequestBody ValidateTicketRequest request
	) {
		TicketValidationResponse response = ticketValidationService.validateTicket(eventId, staffId, request);
		return ResponseEntity.status(201).body(response);
	}

	@Operation(
		summary = "List ticket validations",
		description = "List all validation attempts for the event so staff can audit scans.",
		responses = {
			@ApiResponse(responseCode = "200", description = "Validation attempts returned successfully"),
			@ApiResponse(responseCode = "403", description = "User is not staff or not assigned to the event"),
			@ApiResponse(responseCode = "404", description = "Event not found")
		}
	)
	@GetMapping
	public ResponseEntity<List<TicketValidationResponse>> listValidations(
		@Parameter(description = "Event identifier", required = true)
		@PathVariable("event_id") UUID eventId,
		@Parameter(description = "Staff user identifier", required = true)
		@RequestHeader("X-User-Id") UUID staffId
	) {
		List<TicketValidationResponse> validations = ticketValidationService.listValidations(eventId, staffId);
		return ResponseEntity.ok(validations);
	}
}
