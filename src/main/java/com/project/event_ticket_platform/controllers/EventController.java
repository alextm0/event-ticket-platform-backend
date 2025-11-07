package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.services.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

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
	public List<EventResponse> getEvents() {
		return eventService.getAllEvents();
	}
}
