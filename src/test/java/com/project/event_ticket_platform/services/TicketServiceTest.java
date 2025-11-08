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
	private TicketMapper ticketMapper;

	@Mock
	private QrCodeMapper qrCodeMapper;

	@InjectMocks
	private TicketService ticketService;

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
		when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		TicketOrder savedOrder = new TicketOrder();
		savedOrder.setId(UUID.randomUUID());
		savedOrder.setUser(user);
		savedOrder.setTotalAmount(BigDecimal.valueOf(300.00));
		savedOrder.setStatus(OrderStatus.PAID);
		savedOrder.setTickets(new ArrayList<>());

		when(ticketOrderRepository.save(any(TicketOrder.class))).thenReturn(savedOrder);

		// Act
		PurchaseTicketResponse response = ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);

		// Assert
		assertNotNull(response);
		assertEquals(OrderStatus.PAID, response.orderStatus());
		verify(eventRepository).findById(eventId);
		verify(ticketTypeRepository).findById(ticketTypeId);
		verify(userRepository).findById(userId);
		verify(ticketOrderRepository).save(any(TicketOrder.class));
	}

	@Test
	@DisplayName("Should throw EventNotFoundException when event does not exist")
	void purchaseTicket_EventNotFound() {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EventNotFoundException.class, () -> {
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);
		});
		verify(eventRepository).findById(eventId);
		verify(ticketTypeRepository, never()).findById(any());
	}

	@Test
	@DisplayName("Should throw EventNotPublishedException when event is not published")
	void purchaseTicket_EventNotPublished() {
		// Arrange
		publishedEvent.setStatus(EventStatus.DRAFT);
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));

		// Act & Assert
		assertThrows(EventNotPublishedException.class, () -> {
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);
		});
		verify(eventRepository).findById(eventId);
		verify(ticketTypeRepository, never()).findById(any());
	}

	@Test
	@DisplayName("Should throw TicketTypeNotFoundException when ticket type does not exist")
	void purchaseTicket_TicketTypeNotFound() {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(TicketTypeNotFoundException.class, () -> {
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);
		});
		verify(eventRepository).findById(eventId);
		verify(ticketTypeRepository).findById(ticketTypeId);
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when ticket type does not belong to event")
	void purchaseTicket_TicketTypeNotBelongsToEvent() {
		// Arrange
		Event differentEvent = new Event();
		differentEvent.setId(UUID.randomUUID());
		ticketType.setEvent(differentEvent);

		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);
		});
		verify(eventRepository).findById(eventId);
		verify(ticketTypeRepository).findById(ticketTypeId);
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when ticket type is not active")
	void purchaseTicket_TicketTypeNotActive() {
		// Arrange
		ticketType.setActive(false);
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);
		});
		verify(eventRepository).findById(eventId);
		verify(ticketTypeRepository).findById(ticketTypeId);
	}

	@Test
	@DisplayName("Should throw InsufficientTicketsException when not enough tickets available")
	void purchaseTicket_InsufficientTickets() {
		// Arrange
		ticketType.setSoldCount(99); // Only 1 ticket left
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

		// Act & Assert
		assertThrows(InsufficientTicketsException.class, () -> {
			ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);
		});
		verify(eventRepository).findById(eventId);
		verify(ticketTypeRepository).findById(ticketTypeId);
		verify(ticketOrderRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should update sold count after purchase")
	void purchaseTicket_UpdatesSoldCount() {
		// Arrange
		int initialSoldCount = ticketType.getSoldCount();
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		TicketOrder savedOrder = new TicketOrder();
		savedOrder.setId(UUID.randomUUID());
		savedOrder.setUser(user);
		savedOrder.setTotalAmount(BigDecimal.valueOf(300.00));
		savedOrder.setStatus(OrderStatus.PAID);
		savedOrder.setTickets(new ArrayList<>());

		when(ticketOrderRepository.save(any(TicketOrder.class))).thenReturn(savedOrder);

		// Act
		ticketService.purchaseTicket(eventId, ticketTypeId, request, userId);

		// Assert
		assertEquals(initialSoldCount + 2, ticketType.getSoldCount());
		verify(ticketOrderRepository).save(any(TicketOrder.class));
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
		assertThrows(TicketNotFoundException.class, () -> {
			ticketService.getTicketById(ticketId, userId);
		});
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
		assertThrows(UnauthorizedAccessException.class, () -> {
			ticketService.getTicketById(ticketId, userId);
		});
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
			qrCodeId, "https://api.event-platform.com/qr/" + qrCodeId, QrCodeStatusEnum.ACTIVE
		);

		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
		when(qrCodeMapper.toResponse(qrCode)).thenReturn(qrCodeResponse);

		// Act
		QrCodeResponse result = ticketService.getTicketQrCode(ticketId, userId);

		// Assert
		assertNotNull(result);
		assertEquals(qrCodeId, result.id());
		verify(ticketRepository).findById(ticketId);
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

		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

		// Act & Assert
		assertThrows(UnauthorizedAccessException.class, () -> {
			ticketService.getTicketQrCode(ticketId, userId);
		});
		verify(ticketRepository).findById(ticketId);
		verify(qrCodeMapper, never()).toResponse(any());
	}
}

