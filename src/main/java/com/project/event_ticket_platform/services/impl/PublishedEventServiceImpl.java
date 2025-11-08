package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.PublishedEventResponse;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.EventNotPublishedException;
import com.project.event_ticket_platform.mappers.PublishedEventMapper;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.services.PublishedEventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PublishedEventServiceImpl implements PublishedEventService {

	private final EventRepository eventRepository;
	private final PublishedEventMapper publishedEventMapper;

	public PublishedEventServiceImpl(EventRepository eventRepository, PublishedEventMapper publishedEventMapper) {
		this.eventRepository = eventRepository;
		this.publishedEventMapper = publishedEventMapper;
	}

	@Override
	public Page<PublishedEventResponse> listPublishedEvents(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Event> publishedEvents = eventRepository.findAllByStatus(EventStatus.PUBLISHED, pageable);
		return publishedEvents.map(publishedEventMapper::toResponse);
	}

	@Override
	public PublishedEventResponse getPublishedEventById(UUID eventId) {
		Event event = eventRepository.findById(eventId)
			.orElseThrow(() -> new EventNotFoundException(eventId));

		if (event.getStatus() != EventStatus.PUBLISHED) {
			throw new EventNotPublishedException(eventId);
		}

		return publishedEventMapper.toResponse(event);
	}
}

