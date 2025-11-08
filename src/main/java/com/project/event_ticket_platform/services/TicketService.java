package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.PurchaseTicketRequest;
import com.project.event_ticket_platform.dtos.PurchaseTicketResponse;
import com.project.event_ticket_platform.dtos.QrCodeResponse;
import com.project.event_ticket_platform.dtos.TicketResponse;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.entities.OrderStatus;
import com.project.event_ticket_platform.entities.QrCode;
import com.project.event_ticket_platform.entities.QrCodeStatusEnum;
import com.project.event_ticket_platform.entities.Ticket;
import com.project.event_ticket_platform.entities.TicketOrder;
import com.project.event_ticket_platform.entities.TicketStatus;
import com.project.event_ticket_platform.entities.TicketType;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.EventNotPublishedException;
import com.project.event_ticket_platform.exceptions.InsufficientTicketsException;
import com.project.event_ticket_platform.exceptions.TicketNotFoundException;
import com.project.event_ticket_platform.exceptions.TicketTypeNotActiveException;
import com.project.event_ticket_platform.exceptions.TicketTypeNotBelongsToEventException;
import com.project.event_ticket_platform.exceptions.TicketTypeNotFoundException;
import com.project.event_ticket_platform.exceptions.UnauthorizedAccessException;
import com.project.event_ticket_platform.exceptions.UserNotFoundException;
import com.project.event_ticket_platform.mappers.QrCodeMapper;
import com.project.event_ticket_platform.mappers.TicketMapper;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.repositories.TicketOrderRepository;
import com.project.event_ticket_platform.repositories.TicketRepository;
import com.project.event_ticket_platform.repositories.TicketTypeRepository;
import com.project.event_ticket_platform.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TicketService {

	private final TicketRepository ticketRepository;
	private final TicketTypeRepository ticketTypeRepository;
	private final EventRepository eventRepository;
	private final TicketOrderRepository ticketOrderRepository;
	private final UserRepository userRepository;
	private final TicketMapper ticketMapper;
	private final QrCodeMapper qrCodeMapper;

	public TicketService(TicketRepository ticketRepository,
						 TicketTypeRepository ticketTypeRepository,
						 EventRepository eventRepository,
						 TicketOrderRepository ticketOrderRepository,
						 UserRepository userRepository,
						 TicketMapper ticketMapper,
						 QrCodeMapper qrCodeMapper) {
		this.ticketRepository = ticketRepository;
		this.ticketTypeRepository = ticketTypeRepository;
		this.eventRepository = eventRepository;
		this.ticketOrderRepository = ticketOrderRepository;
		this.userRepository = userRepository;
		this.ticketMapper = ticketMapper;
		this.qrCodeMapper = qrCodeMapper;
	}

	/**
	 * Purchase tickets for a published event
	 */
	@Transactional
	public PurchaseTicketResponse purchaseTicket(UUID eventId, UUID ticketTypeId, PurchaseTicketRequest request, UUID userId) {
		// Validate event exists and is published
		Event event = eventRepository.findById(eventId)
			.orElseThrow(() -> new EventNotFoundException(eventId));

		if (event.getStatus() != EventStatus.PUBLISHED) {
			throw new EventNotPublishedException(eventId);
		}

		// Get user
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException(userId));

		// Load ticket type with pessimistic write lock to prevent race conditions
		// This ensures atomic check-and-update of sold_count
		TicketType ticketType = ticketTypeRepository.findByIdWithLock(ticketTypeId)
			.orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

		if (!ticketType.getEvent().getId().equals(eventId)) {
			throw new TicketTypeNotBelongsToEventException(ticketTypeId, eventId);
		}

		if (!ticketType.isActive()) {
			throw new TicketTypeNotActiveException(ticketTypeId);
		}

		// Check ticket availability with locked entity to prevent overbooking
		int availableTickets = ticketType.getTotalQuantity() - ticketType.getSoldCount();
		if (availableTickets < request.quantity()) {
			throw new InsufficientTicketsException(
				String.format("Only %d tickets available, but %d requested", availableTickets, request.quantity())
			);
		}

		// Create order
		TicketOrder order = new TicketOrder();
		order.setUser(user);
		order.setBuyerName(user.getName());
		order.setBuyerEmail(user.getEmail());
		order.setTotalAmount(ticketType.getPrice().multiply(BigDecimal.valueOf(request.quantity())));
		order.setStatus(OrderStatus.PAID);

		// Create tickets with QR codes
		for (int i = 0; i < request.quantity(); i++) {
			Ticket ticket = new Ticket();
			ticket.setTicketType(ticketType);
			ticket.setStatus(TicketStatus.PURCHASED);

			// Create QR code
			QrCode qrCode = new QrCode();
			qrCode.setStatus(QrCodeStatusEnum.ACTIVE);
			ticket.setQrCode(qrCode);

			order.addTicket(ticket);
		}

		// Update sold count atomically (entity is locked, preventing concurrent modifications)
		ticketType.setSoldCount(ticketType.getSoldCount() + request.quantity());
		ticketTypeRepository.save(ticketType);

		// Save order (cascades to tickets and QR codes)
		TicketOrder savedOrder = ticketOrderRepository.save(order);

		// Map to response
		List<TicketResponse> ticketResponses = savedOrder.getTickets().stream()
			.map(ticketMapper::toResponse)
			.collect(Collectors.toList());

		return new PurchaseTicketResponse(
			savedOrder.getId(),
			savedOrder.getTotalAmount(),
			savedOrder.getStatus(),
			ticketResponses
		);
	}

	/**
	 * List all tickets for a user
	 */
	public List<TicketResponse> listUserTickets(UUID userId) {
		List<Ticket> tickets = ticketRepository.findAllByUserId(userId);
		return tickets.stream()
			.map(ticketMapper::toResponse)
			.collect(Collectors.toList());
	}

	/**
	 * Retrieve a specific ticket for a user
	 */
	public TicketResponse getTicketById(UUID ticketId, UUID userId) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new TicketNotFoundException(ticketId));

		// Verify the ticket belongs to the user
		if (!ticket.getOrder().getUser().getId().equals(userId)) {
			throw new UnauthorizedAccessException("You do not have access to this ticket");
		}

		return ticketMapper.toResponse(ticket);
	}

	/**
	 * Retrieve QR code for a ticket
	 */
	public QrCodeResponse getTicketQrCode(UUID ticketId, UUID userId) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new TicketNotFoundException(ticketId));

		// Verify the ticket belongs to the user
		if (!ticket.getOrder().getUser().getId().equals(userId)) {
			throw new UnauthorizedAccessException("You do not have access to this ticket");
		}

		return qrCodeMapper.toResponse(ticket.getQrCode());
	}
}

