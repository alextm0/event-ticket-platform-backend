package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.dtos.UpdateEventRequest;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.EventValidationException;
import com.project.event_ticket_platform.exceptions.OrganizerNotFoundException;
import com.project.event_ticket_platform.mappers.EventMapper;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.repositories.UserRepository;
import com.project.event_ticket_platform.services.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {

	private final EventRepository eventRepository;
	private final UserRepository userRepository;
	private final EventMapper eventMapper;

	public EventServiceImpl(
		EventRepository eventRepository,
		UserRepository userRepository,
		EventMapper eventMapper
	) {
		this.eventRepository = eventRepository;
		this.userRepository = userRepository;
		this.eventMapper = eventMapper;
	}

	@Override
	@Transactional
	public EventResponse createEvent(CreateEventRequest request) {
		User organizer = userRepository.findById(request.organizerId())
			.orElseThrow(() -> new OrganizerNotFoundException(request.organizerId()));

		Event event = eventMapper.toEntity(request);
		event.setOrganizer(organizer);
		Event savedEvent = eventRepository.save(event);
		return eventMapper.toResponse(savedEvent);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<EventResponse> getAllEvents(Pageable pageable) {
		return eventRepository.findAll(pageable)
			.map(eventMapper::toResponse);
	}

	@Override
	@Transactional
	public EventResponse updateEvent(UUID eventId, UpdateEventRequest request) {
		Event event = eventRepository.findById(eventId)
			.orElseThrow(() -> new EventNotFoundException(eventId));

		validateStatusTransition(event.getStatus(), request.status());

		eventMapper.updateEvent(request, event);

		Event saved = eventRepository.save(event);
		return eventMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public void deleteEvent(UUID eventId) {
		if (!eventRepository.existsById(eventId)) {
			throw new EventNotFoundException(eventId);
		}
		eventRepository.deleteById(eventId);
	}

	private void validateStatusTransition(EventStatus currentStatus, EventStatus requestedStatus) {
		if (requestedStatus == null) {
			throw new EventValidationException("Status is required.");
		}

		if (currentStatus == requestedStatus) {
			if (currentStatus == EventStatus.DRAFT) {
				return;
			}
			throw new EventValidationException("Only draft events can be modified.");
		}

		if (currentStatus == EventStatus.DRAFT && requestedStatus == EventStatus.PUBLISHED) {
			return;
		}

		throw new EventValidationException("Only draft events can transition to PUBLISHED.");
	}
}
