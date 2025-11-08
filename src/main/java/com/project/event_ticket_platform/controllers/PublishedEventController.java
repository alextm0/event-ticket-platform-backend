package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.PublishedEventResponse;
import com.project.event_ticket_platform.services.PublishedEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Published Events", description = "APIs for browsing published events")
@Validated
public class PublishedEventController {

	private final PublishedEventService publishedEventService;

	public PublishedEventController(PublishedEventService publishedEventService) {
		this.publishedEventService = publishedEventService;
	}

	@Operation(
		summary = "List published events",
		description = "Retrieve a paginated list of all published events available for ticket purchase",
		responses = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved published events"),
			@ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
		}
	)
	@GetMapping("/published-events")
	public ResponseEntity<Page<PublishedEventResponse>> listPublishedEvents(
		@Parameter(description = "Page number (0-indexed)", example = "0")
		@RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be 0 or greater") int page,
		@Parameter(description = "Number of items per page", example = "20")
		@RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1") int size
	) {
		Page<PublishedEventResponse> events = publishedEventService.listPublishedEvents(page, size);
		return ResponseEntity.ok(events);
	}

	@Operation(
		summary = "Retrieve published event",
		description = "Retrieve details of a specific published event by ID",
		responses = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved event"),
			@ApiResponse(responseCode = "404", description = "Event not found"),
			@ApiResponse(responseCode = "400", description = "Event not published or invalid request")
		}
	)
	@GetMapping("/published-event/{published_event_id}")
	public ResponseEntity<PublishedEventResponse> getPublishedEvent(
		@Parameter(description = "Published event ID", required = true)
		@PathVariable("published_event_id") UUID publishedEventId
	) {
		PublishedEventResponse event = publishedEventService.getPublishedEventById(publishedEventId);
		return ResponseEntity.ok(event);
	}
}

