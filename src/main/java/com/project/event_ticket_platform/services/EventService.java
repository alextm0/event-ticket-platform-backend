package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;

import java.util.List;

public interface EventService {

	EventResponse createEvent(CreateEventRequest request);

	List<EventResponse> getAllEvents();
}
