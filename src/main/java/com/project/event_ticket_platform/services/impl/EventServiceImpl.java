package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.dtos.EventTicketSaleResponse;
import com.project.event_ticket_platform.dtos.UpdateEventRequest;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.entities.Ticket;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.exceptions.*;
import com.project.event_ticket_platform.mappers.EventMapper;
import com.project.event_ticket_platform.mappers.TicketMapper;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.repositories.TicketRepository;
import com.project.event_ticket_platform.repositories.UserRepository;
import com.project.event_ticket_platform.services.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {

	private final EventRepository eventRepository;
	private final UserRepository userRepository;
	private final TicketRepository ticketRepository;
	private final EventMapper eventMapper;
	private final TicketMapper ticketMapper;

	public EventServiceImpl(
            EventRepository eventRepository,
            UserRepository userRepository, TicketRepository ticketRepository,
            EventMapper eventMapper, TicketMapper ticketMapper
    ) {
		this.eventRepository = eventRepository;
		this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.eventMapper = eventMapper;
        this.ticketMapper = ticketMapper;
    }

	@Override
	@Transactional
	public EventResponse createEvent(CreateEventRequest request) {
		User organizer = userRepository.findById(request.organizerId())
			.orElseThrow(() -> new OrganizerNotFoundException(request.organizerId()));

		Event event = eventMapper.toEntity(request);
		event.setOrganizer(organizer);
		validateNewEvent(event);
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

		if (request.title() != null && request.title().isBlank()) {
			throw new EventValidationException("Event title cannot be blank.");
		}
		if (request.description() != null && request.description().isBlank()) {
			throw new EventValidationException("Event description cannot be blank.");
		}
		if (request.location() != null && request.location().isBlank()) {
			throw new EventValidationException("Event location cannot be blank.");
		}

		EventStatus requestedStatus = request.status() != null ? request.status() : event.getStatus();
		validateStatusTransition(event.getStatus(), requestedStatus);

		boolean startChanged = request.startTime() != null;
		eventMapper.updateEvent(request, event);
		event.setStatus(requestedStatus);
		validateUpdatedEvent(event, startChanged);

		Event saved = eventRepository.save(event);
		return eventMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public void deleteEvent(UUID eventId) {
		Event event = eventRepository.findById(eventId)
			.orElseThrow(() -> new EventNotFoundException(eventId));
		eventRepository.delete(event);
	}

	@Override
	public Page<EventTicketSaleResponse> getTicketSalesForEvent(UUID eventId, Pageable pageable) {
		if (!eventRepository.existsById(eventId)) {
			throw new EventNotFoundException(eventId);
		}
		return ticketRepository.findAllByEventId(eventId, pageable)
				.map(ticketMapper::toEventTicketSaleResponse);
	}

	@Override
	public EventTicketSaleResponse getTicketSaleForEvent(UUID eventId, UUID ticketId) {
		if (!eventRepository.existsById(eventId)) {
			throw new EventNotFoundException(eventId);
		}
		Ticket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new TicketNotFoundException(ticketId));
		if (!ticket.getTicketType().getEvent().getId().equals(eventId)) {
			throw new TicketTypeNotBelongsToEventException(ticket.getTicketType().getId(), eventId);
		}

		return ticketMapper.toEventTicketSaleResponse(ticket);
	}

	private void validateNewEvent(Event event) {
		if (isBlank(event.getTitle())) {
			throw new EventValidationException("Event title is required.");
		}
		if (isBlank(event.getDescription())) {
			throw new EventValidationException("Event description is required.");
		}
		if (isBlank(event.getLocation())) {
			throw new EventValidationException("Event location is required.");
		}

		Instant start = event.getStartTime();
		Instant end = event.getEndTime();
		Instant now = Instant.now();

		if (start == null || end == null) {
			throw new EventValidationException("Start and end times are required.");
		}

		if (!end.isAfter(start)) {
			throw new EventValidationException("Event end time must be after the start time.");
		}

		if (start.isBefore(now)) {
			throw new EventValidationException("Event start time must be in the future.");
		}
	}

	private void validateUpdatedEvent(Event event, boolean startChanged) {
		Instant start = event.getStartTime();
		Instant end = event.getEndTime();

		if (start == null || end == null) {
			throw new EventValidationException("Start and end times are required.");
		}

		if (!end.isAfter(start)) {
			throw new EventValidationException("Event end time must be after the start time.");
		}

		if (startChanged && start.isBefore(Instant.now())) {
			throw new EventValidationException("Updated start time must be in the future.");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private void validateStatusTransition(EventStatus currentStatus, EventStatus requestedStatus) {
		if (requestedStatus == null) {
			throw new EventValidationException("Status is required.");
		}

		if (currentStatus == requestedStatus) {
			return;
		}

		if (currentStatus == EventStatus.DRAFT && requestedStatus == EventStatus.PUBLISHED) {
			return;
		}

		if (currentStatus == EventStatus.PUBLISHED && requestedStatus == EventStatus.CANCELLED) {
			return;
		}

		throw new EventValidationException("Invalid status transition requested.");
	}
}
