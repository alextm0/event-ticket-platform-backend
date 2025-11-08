package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.PublishedEventResponse;
import com.project.event_ticket_platform.services.PublishedEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Published Events", description = "APIs for browsing published events")
public class PublishedEventController {

	private final PublishedEventService publishedEventService;

	public PublishedEventController(PublishedEventService publishedEventService) {
		this.publishedEventService = publishedEventService;
	}

	@Operation(
		summary = "Search published events",
		description = "Retrieve a list of all published events available for ticket purchase",
		responses = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved published events")
		}
	)
	@GetMapping("/published-events")
	public ResponseEntity<List<PublishedEventResponse>> searchPublishedEvents() {
		List<PublishedEventResponse> events = publishedEventService.searchPublishedEvents();
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

