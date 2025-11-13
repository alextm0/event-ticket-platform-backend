package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.TicketValidationResponse;
import com.project.event_ticket_platform.dtos.ValidateTicketRequest;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.Ticket;
import com.project.event_ticket_platform.entities.TicketStatus;
import com.project.event_ticket_platform.entities.TicketValidation;
import com.project.event_ticket_platform.entities.TicketValidationEnum;
import com.project.event_ticket_platform.entities.TicketValidationMethodEnum;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.entities.UserRole;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.QrCodeNotFoundException;
import com.project.event_ticket_platform.exceptions.UnauthorizedAccessException;
import com.project.event_ticket_platform.exceptions.UserNotFoundException;
import com.project.event_ticket_platform.mappers.TicketValidationMapper;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.repositories.EventStaffRepository;
import com.project.event_ticket_platform.repositories.TicketRepository;
import com.project.event_ticket_platform.repositories.TicketValidationRepository;
import com.project.event_ticket_platform.repositories.UserRepository;
import com.project.event_ticket_platform.services.TicketValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketValidationServiceImpl implements TicketValidationService {

	private final TicketRepository ticketRepository;
	private final TicketValidationRepository ticketValidationRepository;
	private final TicketValidationMapper ticketValidationMapper;
	private final EventRepository eventRepository;
	private final EventStaffRepository eventStaffRepository;
	private final UserRepository userRepository;

	public TicketValidationServiceImpl(TicketRepository ticketRepository,
									   TicketValidationRepository ticketValidationRepository,
									   TicketValidationMapper ticketValidationMapper,
									   EventRepository eventRepository,
									   EventStaffRepository eventStaffRepository,
									   UserRepository userRepository) {
		this.ticketRepository = ticketRepository;
		this.ticketValidationRepository = ticketValidationRepository;
		this.ticketValidationMapper = ticketValidationMapper;
		this.eventRepository = eventRepository;
		this.eventStaffRepository = eventStaffRepository;
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public TicketValidationResponse validateTicket(UUID eventId, UUID staffId, ValidateTicketRequest request) {
		if (request == null || request.qrCodeId() == null) {
			throw new IllegalArgumentException("QR code id is required for validation.");
		}

		Event event = loadEvent(eventId);
		validateStaffAccess(eventId, staffId);

		Ticket ticket = ticketRepository.findByQrCodeIdWithEventForUpdate(request.qrCodeId())
			.orElseThrow(() -> new QrCodeNotFoundException(request.qrCodeId()));

		if (!ticket.getTicketType().getEvent().getId().equals(event.getId())) {
			throw new UnauthorizedAccessException("Ticket does not belong to the specified event.");
		}

		TicketValidation validation = new TicketValidation();
		Instant validationTime = Instant.now();
		validation.setTicket(ticket);
		validation.setValidationMethod(TicketValidationMethodEnum.QR_SCAN);
		validation.setValidationDateTime(validationTime);

		if (ticket.getStatus() == TicketStatus.PURCHASED) {
			ticket.setStatus(TicketStatus.CHECKED_IN);
			ticket.setCheckedInAt(validationTime);
			ticketRepository.save(ticket);
			validation.setStatus(TicketValidationEnum.VALID);
		} else {
			validation.setStatus(TicketValidationEnum.INVALID);
		}

		TicketValidation savedValidation = ticketValidationRepository.save(validation);
		return ticketValidationMapper.toResponse(savedValidation);
	}

	@Override
	public List<TicketValidationResponse> listValidations(UUID eventId, UUID staffId) {
		loadEvent(eventId);
		validateStaffAccess(eventId, staffId);

		List<TicketValidation> validations = ticketValidationRepository.findAllByEventIdWithDetails(eventId);
		return validations.stream()
			.map(ticketValidationMapper::toResponse)
			.toList();
	}

	private Event loadEvent(UUID eventId) {
		return eventRepository.findById(eventId)
			.orElseThrow(() -> new EventNotFoundException(eventId));
	}

	private void validateStaffAccess(UUID eventId, UUID staffId) {
		if (staffId == null) {
			throw new UnauthorizedAccessException("Staff user id header is required.");
		}

		User staff = userRepository.findById(staffId)
			.orElseThrow(() -> new UserNotFoundException(staffId));

		if (staff.getRole() != UserRole.STAFF) {
			throw new UnauthorizedAccessException("Only staff members can validate tickets.");
		}

		boolean assigned = eventStaffRepository.existsByEventIdAndStaffId(eventId, staffId);
		if (!assigned) {
			throw new UnauthorizedAccessException("Staff member is not assigned to this event.");
		}
	}
}
