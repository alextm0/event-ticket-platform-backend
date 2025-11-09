package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.PublishedEventResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PublishedEventService {

	/**
	 * List all published events with pagination
	 */
	Page<PublishedEventResponse> listPublishedEvents(int page, int size);

	/**
	 * Retrieve a specific published event by ID
	 */
	PublishedEventResponse getPublishedEventById(UUID eventId);
}
