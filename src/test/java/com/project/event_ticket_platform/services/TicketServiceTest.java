package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.PurchaseTicketRequest;
import com.project.event_ticket_platform.dtos.PurchaseTicketResponse;
import com.project.event_ticket_platform.dtos.QrCodeResponse;
import com.project.event_ticket_platform.dtos.TicketResponse;
import com.project.event_ticket_platform.entities.*;
import com.project.event_ticket_platform.exceptions.*;
import com.project.event_ticket_platform.mappers.QrCodeMapper;
import com.project.event_ticket_platform.mappers.TicketMapper;
import com.project.event_ticket_platform.repositories.*;
import com.project.event_ticket_platform.services.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketTypeRepository ticketTypeRepository;

	@Mock
	private EventRepository eventRepository;

	@Mock
	private TicketOrderRepository ticketOrderRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private QrCodeRepository qrCodeRepository;

	@Mock
	private TicketMapper ticketMapper;

	@Mock
	private QrCodeMapper qrCodeMapper;

	@Mock
	private QrCodeService qrCodeService;

	@InjectMocks
	private TicketServiceImpl ticketService;

	private Event publishedEvent;
	private TicketType ticketType;
	private User user;
	private UUID eventId;
	private UUID ticketTypeId;
	private UUID userId;

	@BeforeEach
	void setUp() {
		eventId = UUID.randomUUID();
		ticketTypeId = UUID.randomUUID();
		userId = UUID.randomUUID();

		user = new User();
		user.setId(userId);
		user.setName("Jane Doe");
		user.setEmail("jane@example.com");

		publishedEvent = new Event();
		publishedEvent.setId(eventId);
		publishedEvent.setTitle("Spring Music Festival");
		publishedEvent.setStatus(EventStatus.PUBLISHED);
		publishedEvent.setTicketTypes(new ArrayList<>());

		ticketType = new TicketType();
		ticketType.setId(ticketTypeId);
		ticketType.setEvent(publishedEvent);
		ticketType.setName("VIP");
		ticketType.setPrice(BigDecimal.valueOf(150.00));
		ticketType.setTotalQuantity(100);
		ticketType.setSoldCount(45);
		ticketType.setActive(true);
		ticketType.setTickets(new ArrayList<>());
	}

	@Test
	@DisplayName("Should successfully purchase tickets")
	void purchaseTicket_Success() {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(ticketTypeRepository.findByIdWithLock(ticketTypeId)).thenReturn(Optional.of(ticketType));

		TicketOrder savedOrder = new TicketOrder();
		savedOrder.setId(UUID.randomUUID());
		savedOrder.setUser(user);
		savedOrder.setTotalAmount(BigDecimal.valueOf(300.00));
		savedOrder.setStatus(OrderStatus.PAID);

		when(ticketOrderRepository.save(any(TicketOrder.class))).thenReturn(savedOrder);
		
		// Mock QR code repository - placeholder QR codes
		when(qrCodeRepository.save(any(QrCode.class))).thenAnswer(invocation -> {
			QrCode qrCode = invocation.getArgument(0);
			if (qrCode.getId() == null) {
				qrCode.setId(UUID.randomUUID());
			}
			return qrCode;
		});
		
		// Mock ticket repository - save tickets
		List<Ticket> savedTickets = new ArrayList<>();
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
			Ticket ticket = invocation.getArgument(0);
			if (ticket.getId() == null) {
				ticket.setId(UUID.randomUUID());
			}
			savedTickets.add(ticket);
			return ticket;
		});
		
		// Mock QR code generation (for real QR codes after tickets are saved)
		when(qrCodeService.generateQrCode(any(Ticket.class))).thenAnswer(invocation -> {
			Ticket ticket = invocation.getArgument(0);
			QrCode qrCode = new QrCode();
			qrCode.setId(UUID.randomUUID());
			qrCode.setCodeData("base64encodedqrcode");
			qrCode.setStatus(com.project.event_ticket_platform.entities.QrCodeStatusEnum.ACTIVE);
			return qrCode;
		});
		
		// Mock fetching tickets with relationships
		when(ticketRepository.findByOrderIdWithRelations(savedOrder.getId())).thenAnswer(invocation -> {
			// Return tickets with all relationships loaded
			List<Ticket> ticketsWithRelations = new ArrayList<>();
			for (Ticket ticket : savedTickets) {
				Ticket ticketWithRelations = new Ticket();
				ticketWithRelations.setId(ticket.getId());
				ticketWithRelations.setTicketType(ticketType);
				ticketWithRelations.setOrder(savedOrder);
				ticketWithRelations.setStatus(TicketStatus.PURCHASED);
				ticketWithRelations.setQrCode(ticket.getQrCode());
				ticketsWithRelations.add(ticketWithRelations);
			}
			return ticketsWithRelations;
		});
		
		// Mock ticket mapper
		when(ticketMapper.toResponse(any(Ticket.class))).thenAnswer(invocation -> {
			Ticket ticket = invocation.getArgument(0);
			return new TicketResponse(
				ticket.getId(), TicketStatus.PURCHASED, "Event", "Location",
				Instant.now(), "VIP", UUID.randomUUID(), null, Instant.now()
			);
		});

		// Act
		PurchaseTicketResponse response = ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);

		// Assert
		assertNotNull(response);
		assertEquals(OrderStatus.PAID, response.orderStatus());
		assertEquals(2, response.tickets().size());
		verify(eventRepository).findById(eventId);
		verify(userRepository).findById(userId);
		verify(ticketTypeRepository).findByIdWithLock(ticketTypeId);
		verify(ticketOrderRepository).save(any(TicketOrder.class));
		verify(qrCodeRepository, times(4)).save(any(QrCode.class)); // 2 placeholders + 2 updates
		verify(ticketRepository, times(2)).save(any(Ticket.class));
		verify(qrCodeService, times(2)).generateQrCode(any(Ticket.class));
		verify(ticketRepository).findByOrderIdWithRelations(savedOrder.getId());
	}

	@Test
	@DisplayName("Should throw EventNotFoundException when event does not exist")
	void purchaseTicket_EventNotFound() {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EventNotFoundException.class, () ->
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId)
		);
		verify(eventRepository).findById(eventId);
		verify(userRepository, never()).findById(any());
		verify(ticketTypeRepository, never()).findByIdWithLock(any());
	}

	@Test
	@DisplayName("Should throw EventNotPublishedException when event is not published")
	void purchaseTicket_EventNotPublished() {
		// Arrange
		publishedEvent.setStatus(EventStatus.DRAFT);
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));

		// Act & Assert
		assertThrows(EventNotPublishedException.class, () ->
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId)
		);
		verify(eventRepository).findById(eventId);
		verify(userRepository, never()).findById(any());
		verify(ticketTypeRepository, never()).findByIdWithLock(any());
	}

	@Test
	@DisplayName("Should throw UserNotFoundException when user does not exist")
	void purchaseTicket_UserNotFound() {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(UserNotFoundException.class, () ->
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId)
		);
		verify(eventRepository).findById(eventId);
		verify(userRepository).findById(userId);
		verify(ticketTypeRepository, never()).findByIdWithLock(any());
	}

	@Test
	@DisplayName("Should throw TicketTypeNotFoundException when ticket type does not exist")
	void purchaseTicket_TicketTypeNotFound() {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(ticketTypeRepository.findByIdWithLock(ticketTypeId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(TicketTypeNotFoundException.class, () ->
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId)
		);
		verify(eventRepository).findById(eventId);
		verify(userRepository).findById(userId);
		verify(ticketTypeRepository).findByIdWithLock(ticketTypeId);
	}

	@Test
	@DisplayName("Should throw TicketTypeNotBelongsToEventException when ticket type does not belong to event")
	void purchaseTicket_TicketTypeNotBelongsToEvent() {
		// Arrange
		Event differentEvent = new Event();
		differentEvent.setId(UUID.randomUUID());
		ticketType.setEvent(differentEvent);

		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(ticketTypeRepository.findByIdWithLock(ticketTypeId)).thenReturn(Optional.of(ticketType));

		// Act & Assert
		assertThrows(TicketTypeNotBelongsToEventException.class, () ->
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId)
		);
		verify(eventRepository).findById(eventId);
		verify(userRepository).findById(userId);
		verify(ticketTypeRepository).findByIdWithLock(ticketTypeId);
	}

	@Test
	@DisplayName("Should throw TicketTypeNotActiveException when ticket type is not active")
	void purchaseTicket_TicketTypeNotActive() {
		// Arrange
		ticketType.setActive(false);
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(ticketTypeRepository.findByIdWithLock(ticketTypeId)).thenReturn(Optional.of(ticketType));

		// Act & Assert
		assertThrows(TicketTypeNotActiveException.class, () ->
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId)
		);
		verify(eventRepository).findById(eventId);
		verify(userRepository).findById(userId);
		verify(ticketTypeRepository).findByIdWithLock(ticketTypeId);
	}

	@Test
	@DisplayName("Should throw InsufficientTicketsException when not enough tickets available")
	void purchaseTicket_InsufficientTickets() {
		// Arrange
		ticketType.setSoldCount(99); // Only 1 ticket left
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(ticketTypeRepository.findByIdWithLock(ticketTypeId)).thenReturn(Optional.of(ticketType));

		// Act & Assert
		assertThrows(InsufficientTicketsException.class, () ->
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId)
		);
		verify(eventRepository).findById(eventId);
		verify(userRepository).findById(userId);
		verify(ticketTypeRepository).findByIdWithLock(ticketTypeId);
		verify(ticketOrderRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should throw QrCodeGenerationException when QR code generation fails")
	void purchaseTicket_QrCodeGenerationFailure() {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(1);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(ticketTypeRepository.findByIdWithLock(ticketTypeId)).thenReturn(Optional.of(ticketType));

		TicketOrder savedOrder = new TicketOrder();
		savedOrder.setId(UUID.randomUUID());
		savedOrder.setUser(user);
		
		when(ticketOrderRepository.save(any(TicketOrder.class))).thenReturn(savedOrder);
		
		// Mock QR code repository for placeholder
		when(qrCodeRepository.save(any(QrCode.class))).thenAnswer(invocation -> {
			QrCode qrCode = invocation.getArgument(0);
			if (qrCode.getId() == null) {
				qrCode.setId(UUID.randomUUID());
			}
			return qrCode;
		});
		
		// Mock ticket repository
		Ticket savedTicket = new Ticket();
		savedTicket.setId(UUID.randomUUID());
		savedTicket.setTicketType(ticketType);
		savedTicket.setOrder(savedOrder);
		QrCode placeholderQrCode = new QrCode();
		placeholderQrCode.setId(UUID.randomUUID());
		savedTicket.setQrCode(placeholderQrCode);
		
		when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

		// Mock QR code generation to fail
		when(qrCodeService.generateQrCode(any(Ticket.class)))
			.thenThrow(new QrCodeGenerationException("Failed to generate QR code"));

		// Act & Assert
		assertThrows(QrCodeGenerationException.class, () -> {
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);
		});
		verify(qrCodeService).generateQrCode(any(Ticket.class));
	}

	@Test
	@DisplayName("Should update sold count after purchase")
	void purchaseTicket_UpdatesSoldCount() {
		// Arrange
		int initialSoldCount = ticketType.getSoldCount();
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(ticketTypeRepository.findByIdWithLock(ticketTypeId)).thenReturn(Optional.of(ticketType));

		TicketOrder savedOrder = new TicketOrder();
		savedOrder.setId(UUID.randomUUID());
		savedOrder.setUser(user);
		savedOrder.setTotalAmount(BigDecimal.valueOf(300.00));
		savedOrder.setStatus(OrderStatus.PAID);

		when(ticketOrderRepository.save(any(TicketOrder.class))).thenReturn(savedOrder);
		
		// Mock QR code repository
		when(qrCodeRepository.save(any(QrCode.class))).thenAnswer(invocation -> {
			QrCode qrCode = invocation.getArgument(0);
			if (qrCode.getId() == null) {
				qrCode.setId(UUID.randomUUID());
			}
			return qrCode;
		});
		
		// Mock ticket repository
		List<Ticket> savedTickets = new ArrayList<>();
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
			Ticket ticket = invocation.getArgument(0);
			if (ticket.getId() == null) {
				ticket.setId(UUID.randomUUID());
			}
			savedTickets.add(ticket);
			return ticket;
		});
		
		// Mock QR code generation
		when(qrCodeService.generateQrCode(any(Ticket.class))).thenAnswer(invocation -> {
			Ticket ticket = invocation.getArgument(0);
			QrCode qrCode = new QrCode();
			qrCode.setId(UUID.randomUUID());
			qrCode.setCodeData("base64encodedqrcode");
			qrCode.setStatus(com.project.event_ticket_platform.entities.QrCodeStatusEnum.ACTIVE);
			return qrCode;
		});
		
		// Mock fetching tickets with relationships
		when(ticketRepository.findByOrderIdWithRelations(savedOrder.getId())).thenAnswer(invocation -> {
			return savedTickets;
		});
		
		// Mock ticket mapper
		when(ticketMapper.toResponse(any(Ticket.class))).thenAnswer(invocation -> {
			Ticket ticket = invocation.getArgument(0);
			return new TicketResponse(
				ticket.getId(), TicketStatus.PURCHASED, "Event", "Location",
				Instant.now(), "VIP", UUID.randomUUID(), null, Instant.now()
			);
		});

		// Act
		ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);

		// Assert
		assertEquals(initialSoldCount + 2, ticketType.getSoldCount());
		verify(ticketTypeRepository).save(ticketType);
		verify(ticketOrderRepository).save(any(TicketOrder.class));
		verify(qrCodeService, times(2)).generateQrCode(any(Ticket.class));
	}

	@Test
	@DisplayName("Should list all tickets for a user")
	void listUserTickets_Success() {
		// Arrange
		Ticket ticket1 = new Ticket();
		ticket1.setId(UUID.randomUUID());
		Ticket ticket2 = new Ticket();
		ticket2.setId(UUID.randomUUID());
		List<Ticket> tickets = List.of(ticket1, ticket2);

		when(ticketRepository.findAllByUserId(userId)).thenReturn(tickets);
		when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(
			new TicketResponse(UUID.randomUUID(), TicketStatus.PURCHASED, "Event", "Location",
				Instant.now(), "VIP", UUID.randomUUID(), null, Instant.now())
		);

		// Act
		List<TicketResponse> result = ticketService.listUserTickets(userId);

		// Assert
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(ticketRepository).findAllByUserId(userId);
		verify(ticketMapper, times(2)).toResponse(any(Ticket.class));
	}

	@Test
	@DisplayName("Should return ticket by ID for authorized user")
	void getTicketById_Success() {
		// Arrange
		UUID ticketId = UUID.randomUUID();
		Ticket ticket = new Ticket();
		ticket.setId(ticketId);
		TicketOrder order = new TicketOrder();
		order.setUser(user);
		ticket.setOrder(order);

		TicketResponse ticketResponse = new TicketResponse(
			ticketId, TicketStatus.PURCHASED, "Event", "Location",
			Instant.now(), "VIP", UUID.randomUUID(), null, Instant.now()
		);

		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
		when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

		// Act
		TicketResponse result = ticketService.getTicketById(ticketId, userId);

		// Assert
		assertNotNull(result);
		assertEquals(ticketId, result.id());
		verify(ticketRepository).findById(ticketId);
		verify(ticketMapper).toResponse(ticket);
	}

	@Test
	@DisplayName("Should throw TicketNotFoundException when ticket does not exist")
	void getTicketById_TicketNotFound() {
		// Arrange
		UUID ticketId = UUID.randomUUID();
		when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(TicketNotFoundException.class, () ->
			ticketService.getTicketById(ticketId, userId)
		);
		verify(ticketRepository).findById(ticketId);
		verify(ticketMapper, never()).toResponse(any());
	}

	@Test
	@DisplayName("Should throw UnauthorizedAccessException when user does not own ticket")
	void getTicketById_UnauthorizedAccess() {
		// Arrange
		UUID ticketId = UUID.randomUUID();
		Ticket ticket = new Ticket();
		ticket.setId(ticketId);
		User differentUser = new User();
		differentUser.setId(UUID.randomUUID());
		TicketOrder order = new TicketOrder();
		order.setUser(differentUser);
		ticket.setOrder(order);

		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

		// Act & Assert
		assertThrows(UnauthorizedAccessException.class, () ->
			ticketService.getTicketById(ticketId, userId)
		);
		verify(ticketRepository).findById(ticketId);
		verify(ticketMapper, never()).toResponse(any());
	}

	@Test
	@DisplayName("Should return QR code for authorized user")
	void getTicketQrCode_Success() {
		// Arrange
		UUID ticketId = UUID.randomUUID();
		UUID qrCodeId = UUID.randomUUID();

		QrCode qrCode = new QrCode();
		qrCode.setId(qrCodeId);

		Ticket ticket = new Ticket();
		ticket.setId(ticketId);
		ticket.setQrCode(qrCode);

		TicketOrder order = new TicketOrder();
		order.setUser(user);
		ticket.setOrder(order);

		QrCodeResponse qrCodeResponse = new QrCodeResponse(
			qrCodeId,
			"https://api.event-platform.com/qr/" + qrCodeId,
			QrCodeStatusEnum.ACTIVE,
			Instant.now()
		);

		when(ticketRepository.findByIdWithQrCode(ticketId)).thenReturn(Optional.of(ticket));
		when(qrCodeMapper.toResponse(qrCode)).thenReturn(qrCodeResponse);

		// Act
		QrCodeResponse result = ticketService.getTicketQrCode(ticketId, userId);

		// Assert
		assertNotNull(result);
		assertEquals(qrCodeId, result.id());
		verify(ticketRepository).findByIdWithQrCode(ticketId);
		verify(qrCodeMapper).toResponse(qrCode);
	}

	@Test
	@DisplayName("Should throw UnauthorizedAccessException when getting QR code for ticket not owned")
	void getTicketQrCode_UnauthorizedAccess() {
		// Arrange
		UUID ticketId = UUID.randomUUID();

		Ticket ticket = new Ticket();
		ticket.setId(ticketId);

		User differentUser = new User();
		differentUser.setId(UUID.randomUUID());

		TicketOrder order = new TicketOrder();
		order.setUser(differentUser);
		ticket.setOrder(order);

		when(ticketRepository.findByIdWithQrCode(ticketId)).thenReturn(Optional.of(ticket));

		// Act & Assert
		assertThrows(UnauthorizedAccessException.class, () ->
			ticketService.getTicketQrCode(ticketId, userId)
		);
		verify(ticketRepository).findByIdWithQrCode(ticketId);
		verify(qrCodeMapper, never()).toResponse(any());
	}

	@Test
	@DisplayName("Should throw TicketNotFoundException when ticket does not exist for QR code retrieval")
	void getTicketQrCode_TicketNotFound() {
		// Arrange
		UUID ticketId = UUID.randomUUID();
		when(ticketRepository.findByIdWithQrCode(ticketId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(TicketNotFoundException.class, () ->
			ticketService.getTicketQrCode(ticketId, userId)
		);
		verify(ticketRepository).findByIdWithQrCode(ticketId);
		verify(qrCodeMapper, never()).toResponse(any());
	}
}

