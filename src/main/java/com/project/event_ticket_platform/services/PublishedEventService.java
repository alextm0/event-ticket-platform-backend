package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.PublishedEventResponse;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.EventNotPublishedException;
import com.project.event_ticket_platform.mappers.PublishedEventMapper;
import com.project.event_ticket_platform.repositories.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PublishedEventService {

	private final EventRepository eventRepository;
	private final PublishedEventMapper publishedEventMapper;

	public PublishedEventService(EventRepository eventRepository, PublishedEventMapper publishedEventMapper) {
		this.eventRepository = eventRepository;
		this.publishedEventMapper = publishedEventMapper;
	}

	/**
	 * Search all published events
	 */
	public List<PublishedEventResponse> searchPublishedEvents() {
		List<Event> publishedEvents = eventRepository.findAllByStatus(EventStatus.PUBLISHED);
		return publishedEvents.stream()
			.map(publishedEventMapper::toResponse)
			.collect(Collectors.toList());
	}

	/**
	 * Retrieve a specific published event by ID
	 */
	public PublishedEventResponse getPublishedEventById(UUID eventId) {
		Event event = eventRepository.findByIdWithTicketTypes(eventId)
			.orElseThrow(() -> new EventNotFoundException(eventId));

		if (event.getStatus() != EventStatus.PUBLISHED) {
			throw new EventNotPublishedException(eventId);
		}

		return publishedEventMapper.toResponse(event);
	}
}

