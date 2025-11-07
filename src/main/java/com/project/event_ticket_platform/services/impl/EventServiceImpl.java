package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.exceptions.OrganizerNotFoundException;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.repositories.UserRepository;
import com.project.event_ticket_platform.services.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {

	private final EventRepository eventRepository;
	private final UserRepository userRepository;

	public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
		this.eventRepository = eventRepository;
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public EventResponse createEvent(CreateEventRequest request) {
		User organizer = userRepository.findById(request.organizerId())
			.orElseThrow(() -> new OrganizerNotFoundException(request.organizerId()));

		Event event = new Event();
		event.setOrganizer(organizer);
		event.setTitle(request.title());
		event.setDescription(request.description());
		event.setLocation(request.location());
		event.setStartTime(request.startTime());
		event.setEndTime(request.endTime());
		event.setStatus(request.status() != null ? request.status() : EventStatus.DRAFT);

		Event savedEvent = eventRepository.save(event);
		return toResponse(savedEvent);
	}

	@Override
	@Transactional(readOnly = true)
	public List<EventResponse> getAllEvents() {
		return eventRepository.findAll()
			.stream()
			.map(this::toResponse)
			.toList();
	}

	private EventResponse toResponse(Event event) {
		UUID organizerId = event.getOrganizer() != null ? event.getOrganizer().getId() : null;
		return new EventResponse(
			event.getId(),
			organizerId,
			event.getTitle(),
			event.getDescription(),
			event.getLocation(),
			event.getStartTime(),
			event.getEndTime(),
			event.getStatus(),
			event.getCreatedAt(),
			event.getUpdatedAt()
		);
	}
}
