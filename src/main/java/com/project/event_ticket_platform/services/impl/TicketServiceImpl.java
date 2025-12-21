package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.PurchaseTicketRequest;
import com.project.event_ticket_platform.dtos.PurchaseTicketResponse;
import com.project.event_ticket_platform.dtos.QrCodeResponse;
import com.project.event_ticket_platform.dtos.TicketResponse;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.entities.OrderStatus;
import com.project.event_ticket_platform.entities.QrCode;
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
import com.project.event_ticket_platform.repositories.QrCodeRepository;
import com.project.event_ticket_platform.repositories.TicketOrderRepository;
import com.project.event_ticket_platform.repositories.TicketRepository;
import com.project.event_ticket_platform.repositories.TicketTypeRepository;
import com.project.event_ticket_platform.repositories.UserRepository;
import com.project.event_ticket_platform.services.QrCodeService;
import com.project.event_ticket_platform.services.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

	private final TicketRepository ticketRepository;
	private final TicketTypeRepository ticketTypeRepository;
	private final EventRepository eventRepository;
	private final TicketOrderRepository ticketOrderRepository;
	private final UserRepository userRepository;
	private final TicketMapper ticketMapper;
	private final QrCodeMapper qrCodeMapper;
	private final QrCodeService qrCodeService;
	private final QrCodeRepository qrCodeRepository;

	public TicketServiceImpl(TicketRepository ticketRepository,
			TicketTypeRepository ticketTypeRepository,
			EventRepository eventRepository,
			TicketOrderRepository ticketOrderRepository,
			UserRepository userRepository,
			TicketMapper ticketMapper,
			QrCodeMapper qrCodeMapper,
			QrCodeService qrCodeService,
			QrCodeRepository qrCodeRepository) {
		this.ticketRepository = ticketRepository;
		this.ticketTypeRepository = ticketTypeRepository;
		this.eventRepository = eventRepository;
		this.ticketOrderRepository = ticketOrderRepository;
		this.userRepository = userRepository;
		this.ticketMapper = ticketMapper;
		this.qrCodeMapper = qrCodeMapper;
		this.qrCodeService = qrCodeService;
		this.qrCodeRepository = qrCodeRepository;
	}

	@Override
	@Transactional
	public PurchaseTicketResponse purchaseTicket(UUID eventId, UUID ticketTypeId, PurchaseTicketRequest request,
			UUID userId) {
		Integer quantity = request.quantity();
		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be at least 1");
		}
		int requestedQuantity = quantity;

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
		if (availableTickets < requestedQuantity) {
			throw new InsufficientTicketsException(ticketTypeId, requestedQuantity, availableTickets);
		}

		// Create order
		TicketOrder order = new TicketOrder();
		order.setUser(user);
		order.setBuyerName(user.getName());

		// Validate and set buyer email (trim whitespace to handle dirty data)
		String buyerEmail = user.getEmail();
		if (buyerEmail == null || buyerEmail.trim().isEmpty()) {
			throw new IllegalArgumentException("User email is required for ticket purchase");
		}
		// Trim whitespace and tabs from email
		buyerEmail = buyerEmail.trim();
		order.setBuyerEmail(buyerEmail);

		order.setTotalAmount(ticketType.getPrice().multiply(BigDecimal.valueOf(requestedQuantity)));
		order.setStatus(OrderStatus.PAID);

		// Save order first to get order ID (needed for ticket references)
		TicketOrder savedOrder = ticketOrderRepository.save(order);

		// Create tickets - we'll save them first, then generate QR codes
		// But tickets require QR codes, so we create placeholder QR codes first
		List<Ticket> tickets = new java.util.ArrayList<>();

		for (int i = 0; i < requestedQuantity; i++) {
			// Create placeholder QR code first (required by foreign key)
			QrCode placeholderQrCode = new QrCode();
			placeholderQrCode.setCodeData("PLACEHOLDER");
			placeholderQrCode.setStatus(com.project.event_ticket_platform.entities.QrCodeStatusEnum.ACTIVE);
			// generatedDateTime will be set automatically by @CreatedDate
			QrCode savedQrCode = qrCodeRepository.save(placeholderQrCode);

			// Create ticket with placeholder QR code
			Ticket ticket = new Ticket();
			ticket.setTicketType(ticketType);
			ticket.setStatus(TicketStatus.PURCHASED);
			ticket.setOrder(savedOrder);
			ticket.setQrCode(savedQrCode);

			// Save ticket (JPA will generate ID)
			Ticket savedTicket = ticketRepository.save(ticket);
			tickets.add(savedTicket);
		}

		// Now generate real QR codes for each ticket and update them
		for (Ticket ticket : tickets) {
			QrCode realQrCode = qrCodeService.generateQrCode(ticket);
			QrCode existingQrCode = ticket.getQrCode();
			existingQrCode.setCodeData(realQrCode.getCodeData());
			qrCodeRepository.save(existingQrCode);
		}

		// Update sold count atomically (entity is locked, preventing concurrent
		// modifications)
		ticketType.setSoldCount(ticketType.getSoldCount() + requestedQuantity);
		ticketTypeRepository.save(ticketType);

		// Fetch tickets with all relationships for response mapping
		List<Ticket> ticketsWithRelations = ticketRepository.findByOrderIdWithRelations(savedOrder.getId());

		// Map to response
		List<TicketResponse> ticketResponses = ticketsWithRelations.stream()
				.map(ticketMapper::toResponse)
				.collect(Collectors.toList());

		return new PurchaseTicketResponse(
				savedOrder.getId(),
				savedOrder.getTotalAmount(),
				savedOrder.getStatus(),
				ticketResponses);
	}

	@Override
	public List<TicketResponse> listUserTickets(UUID userId) {
		List<Ticket> tickets = ticketRepository.findAllByUserId(userId);
		return tickets.stream()
				.map(ticketMapper::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public TicketResponse getTicketById(UUID ticketId, UUID userId) {
		Ticket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new TicketNotFoundException(ticketId));

		User requestUser = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		// Verify the ticket belongs to the user OR the user is a STAFF member
		boolean isOwner = ticket.getOrder().getUser().getId().equals(userId);
		boolean isStaff = requestUser.getRole() == com.project.event_ticket_platform.entities.UserRole.STAFF;

		if (!isOwner && !isStaff) {
			throw new UnauthorizedAccessException("You do not have access to this ticket");
		}

		return ticketMapper.toResponse(ticket);
	}

	@Override
	public QrCodeResponse getTicketQrCode(UUID ticketId, UUID userId) {
		// Fetch ticket with QR code relationship using JOIN FETCH to avoid N+1 query
		Ticket ticket = ticketRepository.findByIdWithQrCode(ticketId)
				.orElseThrow(() -> new TicketNotFoundException(ticketId));

		// Verify the ticket belongs to the user
		if (!ticket.getOrder().getUser().getId().equals(userId)) {
			throw new UnauthorizedAccessException("You do not have access to this ticket");
		}

		// QR code is already loaded via JOIN FETCH
		QrCode qrCode = ticket.getQrCode();
		if (qrCode == null) {
			throw new IllegalStateException("QR code not found for ticket: " + ticketId);
		}

		return qrCodeMapper.toResponse(qrCode);
	}
}
