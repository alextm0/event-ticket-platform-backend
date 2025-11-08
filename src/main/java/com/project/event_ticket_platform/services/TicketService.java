package com.project.event_ticket_platform.services;

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
	private final QrCodeService qrCodeService;
	private final TicketMapper ticketMapper;
	private final QrCodeMapper qrCodeMapper;

	public TicketService(
		TicketRepository ticketRepository,
		TicketTypeRepository ticketTypeRepository,
		EventRepository eventRepository,
		TicketOrderRepository ticketOrderRepository,
		UserRepository userRepository,
		QrCodeService qrCodeService,
		TicketMapper ticketMapper,
		QrCodeMapper qrCodeMapper
	) {
		this.ticketRepository = ticketRepository;
		this.ticketTypeRepository = ticketTypeRepository;
		this.eventRepository = eventRepository;
		this.ticketOrderRepository = ticketOrderRepository;
		this.userRepository = userRepository;
		this.qrCodeService = qrCodeService;
		this.ticketMapper = ticketMapper;
		this.qrCodeMapper = qrCodeMapper;
	}

	/**
	 * Purchase tickets for a published event
	 */
	@Transactional
	public PurchaseTicketResponse purchaseTickets(
		UUID publishedEventId,
		UUID ticketTypeId,
		UUID userId,
		PurchaseTicketRequest request
	) {
		// 1. Find and validate user
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException(userId));

		// 2. Validate event exists and is published
		Event event = eventRepository.findById(publishedEventId)
			.orElseThrow(() -> new EventNotFoundException(publishedEventId));

		if (event.getStatus() != EventStatus.PUBLISHED) {
			throw new EventNotPublishedException(publishedEventId);
		}

		// 3. Find and lock ticket type (prevents race conditions)
		TicketType ticketType = ticketTypeRepository.findByIdWithLock(ticketTypeId)
			.orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

		// 4. Validate ticket type belongs to the event
		if (!ticketType.getEvent().getId().equals(publishedEventId)) {
			throw new TicketTypeNotBelongsToEventException(ticketTypeId, publishedEventId);
		}

		// 5. Check if ticket type is active
		if (!ticketType.isActive()) {
			throw new TicketTypeNotActiveException(ticketTypeId);
		}

		// 6. Check ticket availability
		int availableTickets = ticketType.getTotalQuantity() - ticketType.getSoldCount();
		if (availableTickets < request.quantity()) {
			throw new InsufficientTicketsException(ticketTypeId, request.quantity(), availableTickets);
		}

		// 7. Create ticket order
		TicketOrder ticketOrder = new TicketOrder();
		ticketOrder.setUser(user);
		ticketOrder.setBuyerName(user.getName());
		ticketOrder.setBuyerEmail(user.getEmail());
		ticketOrder.setTotalAmount(ticketType.getPrice().multiply(BigDecimal.valueOf(request.quantity())));
		ticketOrder.setStatus(OrderStatus.PAID);

		ticketOrder = ticketOrderRepository.save(ticketOrder);

		// 8. Create individual tickets with QR codes
		List<Ticket> tickets = new java.util.ArrayList<>();
		for (int i = 0; i < request.quantity(); i++) {
			// Pre-generate ticket ID so we can use it in QR code generation
			UUID ticketId = UUID.randomUUID();
			
			Ticket ticket = new Ticket();
			ticket.setId(ticketId);
			ticket.setOrder(ticketOrder);
			ticket.setTicketType(ticketType);
			ticket.setStatus(TicketStatus.PURCHASED);

			// Generate and save QR code (needs ticket ID for QR data)
			QrCode qrCode = qrCodeService.generateQrCode(ticket);
			ticket.setQrCode(qrCode);

			// Save ticket with QR code
			ticket = ticketRepository.save(ticket);
			tickets.add(ticket);
		}

		// 9. Update sold count
		ticketType.setSoldCount(ticketType.getSoldCount() + request.quantity());
		ticketTypeRepository.save(ticketType);

		// 10. Map tickets to responses
		List<TicketResponse> ticketResponses = tickets.stream()
			.map(ticketMapper::toResponse)
			.collect(Collectors.toList());

		// 11. Return response
		return new PurchaseTicketResponse(
			ticketOrder.getId(),
			ticketOrder.getTotalAmount(),
			ticketOrder.getStatus(),
			ticketResponses,
			ticketOrder.getCreatedAt()
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
		Ticket ticket = ticketRepository.findByIdWithRelations(ticketId)
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
		Ticket ticket = ticketRepository.findByIdWithRelations(ticketId)
			.orElseThrow(() -> new TicketNotFoundException(ticketId));

		// Verify the ticket belongs to the user
		if (!ticket.getOrder().getUser().getId().equals(userId)) {
			throw new UnauthorizedAccessException("You do not have access to this ticket");
		}

		return qrCodeMapper.toResponse(ticket.getQrCode());
	}
}
