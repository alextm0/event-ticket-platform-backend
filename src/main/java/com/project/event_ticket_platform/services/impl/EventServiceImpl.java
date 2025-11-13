package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.*;
import com.project.event_ticket_platform.entities.*;
import com.project.event_ticket_platform.exceptions.*;
import com.project.event_ticket_platform.mappers.EventMapper;
import com.project.event_ticket_platform.mappers.TicketMapper;
import com.project.event_ticket_platform.mappers.TicketTypeMapper;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.repositories.TicketRepository;
import com.project.event_ticket_platform.repositories.TicketTypeRepository;
import com.project.event_ticket_platform.repositories.UserRepository;
import com.project.event_ticket_platform.services.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {

	private final EventRepository eventRepository;
	private final UserRepository userRepository;
	private final TicketRepository ticketRepository;
	private final TicketTypeRepository ticketTypeRepository;
	private final EventMapper eventMapper;
	private final TicketMapper ticketMapper;
	private final TicketTypeMapper ticketTypeMapper;

	/**
     * Creates a new EventServiceImpl with the required repositories and mappers.
     */
    public EventServiceImpl(
            EventRepository eventRepository,
            UserRepository userRepository, TicketRepository ticketRepository, TicketTypeRepository ticketTypeRepository,
            EventMapper eventMapper, TicketMapper ticketMapper, TicketTypeMapper ticketTypeMapper
    ) {
		this.eventRepository = eventRepository;
		this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.eventMapper = eventMapper;
        this.ticketMapper = ticketMapper;
        this.ticketTypeMapper = ticketTypeMapper;
    }

	/**
	 * Creates and persists a new event associated with the specified organizer.
	 *
	 * @param request the details of the event to create, including the organizer ID
	 * @return the persisted event represented as an EventResponse
	 * @throws OrganizerNotFoundException if no user exists with the given organizer ID
	 * @throws EventValidationException   if the provided event data fails business validations
	 */
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

	/**
	 * Deletes the event identified by the provided ID.
	 *
	 * @param eventId the UUID of the event to delete
	 * @throws EventNotFoundException if no event exists with the given ID
	 */
	@Override
	@Transactional
	public void deleteEvent(UUID eventId) {
		Event event = eventRepository.findById(eventId)
			.orElseThrow(() -> new EventNotFoundException(eventId));
		eventRepository.delete(event);
	}

	/**
	 * Fetches paginated ticket sales for the specified event.
	 *
	 * @param eventId the UUID of the event whose ticket sales to retrieve
	 * @param pageable paging and sorting information
	 * @return a page of EventTicketSaleResponse objects representing the event's ticket sales
	 * @throws EventNotFoundException if no event exists with the provided `eventId`
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<EventTicketSaleResponse> getTicketSalesForEvent(UUID eventId, Pageable pageable) {
		if (!eventRepository.existsById(eventId)) {
			throw new EventNotFoundException(eventId);
		}
		return ticketRepository.findAllByEventId(eventId, pageable)
				.map(ticketMapper::toEventTicketSaleResponse);
	}

	/**
	 * Fetches the sale details for a specific ticket that belongs to a given event.
	 *
	 * @param eventId the UUID of the event to validate ownership against
	 * @param ticketId the UUID of the ticket to retrieve
	 * @return the sale details for the specified ticket and event
	 * @throws EventNotFoundException if no event exists with the given `eventId`
	 * @throws TicketNotFoundException if no ticket exists with the given `ticketId`
	 * @throws TicketTypeNotBelongsToEventException if the ticket's ticket type does not belong to the specified event
	 */
	@Override
	@Transactional(readOnly = true)
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

	/**
	 * Retrieves paginated ticket types belonging to the specified event.
	 *
	 * @param eventId  the UUID of the event
	 * @param pageable pagination and sorting information
	 * @return         a page of TicketTypeResponse objects for the event
	 * @throws EventNotFoundException if no event exists with the given id
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<TicketTypeResponse> getTicketTypesForEvent(UUID eventId, Pageable pageable) {
		if (!eventRepository.existsById(eventId)) {
			throw new EventNotFoundException(eventId);
		}
		return ticketTypeRepository.findByEventId(eventId, pageable)
				.map(ticketTypeMapper::toResponse);
	}

	/**
	 * Fetches the ticket type that belongs to the specified event.
	 *
	 * @param eventId the ID of the event to validate ownership
	 * @param ticketTypeId the ID of the ticket type to retrieve
	 * @return the ticket type mapped to a TicketTypeResponse
	 * @throws EventNotFoundException if no event exists with the given eventId
	 * @throws TicketTypeNotFoundException if no ticket type exists with the given ticketTypeId
	 * @throws TicketTypeNotBelongsToEventException if the ticket type does not belong to the specified event
	 */
	@Override
	@Transactional(readOnly = true)
	public TicketTypeResponse getTicketTypeForEvent(UUID eventId, UUID ticketTypeId) {
		if (!eventRepository.existsById(eventId)) {
			throw new EventNotFoundException(eventId);
		}
		TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
				.orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

		if (!ticketType.getEvent().getId().equals(eventId)) {
			throw new TicketTypeNotBelongsToEventException(ticketTypeId, eventId);
		}

		return ticketTypeMapper.toResponse(ticketType);
	}

	/**
	 * Deletes a ticket type from an event.
	 *
	 * Validates that the event and ticket type exist, that the ticket type belongs to the specified event,
	 * and that no tickets have been sold for that ticket type before deleting it.
	 *
	 * @param eventId      the UUID of the event
	 * @param ticketTypeId the UUID of the ticket type to delete
	 * @throws EventNotFoundException                   if no event with the given `eventId` exists
	 * @throws TicketTypeNotFoundException              if no ticket type with the given `ticketTypeId` exists
	 * @throws TicketTypeNotBelongsToEventException     if the ticket type does not belong to the specified event
	 * @throws TicketTypeInUseException                 if the ticket type has one or more associated tickets
	 */
	@Override
	public void deleteTicketTypeForEvent(UUID eventId, UUID ticketTypeId) {
		if (!eventRepository.existsById(eventId)) {
			throw new EventNotFoundException(eventId);
		}
		TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
				.orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

		if (!ticketType.getEvent().getId().equals(eventId)) {
			throw new TicketTypeNotBelongsToEventException(ticketTypeId, eventId);
		}

		if (!ticketType.getTickets().isEmpty()) {
			throw new TicketTypeInUseException(ticketTypeId);
		}

		ticketTypeRepository.delete(ticketType);
	}

	/**
	 * Applies partial updates to a ticket type belonging to the specified event and returns the updated representation.
	 *
	 * @param eventId      the UUID of the event that must own the ticket type
	 * @param ticketTypeId the UUID of the ticket type to update
	 * @param request      the patch request containing optional fields to apply (name, description, price, quantity, active)
	 * @return             the updated TicketTypeResponse reflecting saved changes
	 * @throws EventNotFoundException                     if no event exists with the given eventId
	 * @throws TicketTypeNotFoundException                if no ticket type exists with the given ticketTypeId
	 * @throws TicketTypeNotBelongsToEventException       if the ticket type does not belong to the specified event
	 * @throws EventValidationException                   if any provided field fails validation (blank name/description, negative price,
	 *                                                   or quantity less than tickets already sold)
	 */
	@Override
	@Transactional
	public TicketTypeResponse patchTicketTypeForEvent(UUID eventId, UUID ticketTypeId, PatchTicketTypeRequest request) {
		if (!eventRepository.existsById(eventId)) {
			throw new EventNotFoundException(eventId);
		}
		TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
				.orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

		if (!ticketType.getEvent().getId().equals(eventId)) {
			throw new TicketTypeNotBelongsToEventException(ticketTypeId, eventId);
		}

		request.name().ifPresent(name -> {
			if (name.isBlank()) {
				throw new EventValidationException("Ticket type name cannot be blank.");
			}
			ticketType.setName(name);
		});

		request.description().ifPresent(description -> {
			if (description.isBlank()) {
				throw new EventValidationException("Ticket type description cannot be blank.");
			}
			ticketType.setDescription(description);
		});

		request.price().ifPresent(price -> {
			if (price.compareTo(BigDecimal.ZERO) < 0) {
				throw new EventValidationException("Ticket type price cannot be negative.");
			}
			ticketType.setPrice(price);
		});

		request.quantity().ifPresent(quantity -> {
			int soldTickets = ticketType.getTickets().size();
			if (quantity < soldTickets) {
				throw new EventValidationException("Quantity cannot be less than the number of tickets already sold (" + soldTickets + ").");
			}
			ticketType.setTotalQuantity(quantity);
		});

		request.active().ifPresent(active -> {
			ticketType.setActive(active);
		});

		TicketType savedTicketType = ticketTypeRepository.save(ticketType);
		return ticketTypeMapper.toResponse(savedTicketType);
	}

	/**
	 * Validates that a newly created Event contains required fields and a valid timeline.
	 *
	 * <p>Checks that title, description, and location are not blank; that start and end
	 * times are present; that the end time is after the start time; and that the start
	 * time is in the future.</p>
	 *
	 * @param event the Event to validate
	 * @throws EventValidationException if any required field is missing or any time constraint is violated
	 */
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