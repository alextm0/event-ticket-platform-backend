package com.project.event_ticket_platform.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.event_ticket_platform.dtos.*;
import com.project.event_ticket_platform.entities.OrderStatus;
import com.project.event_ticket_platform.entities.QrCodeStatusEnum;
import com.project.event_ticket_platform.entities.TicketStatus;
import com.project.event_ticket_platform.exceptions.*;
import com.project.event_ticket_platform.services.TicketService;
import com.project.event_ticket_platform.services.PdfTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TicketController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TicketService ticketService;

	@MockitoBean
	private PdfTicketService pdfTicketService;

	@MockitoBean
	private com.project.event_ticket_platform.services.JwtService jwtService;

	private UUID eventId;
	private UUID ticketTypeId;
	private UUID userId;
	private UUID ticketId;
	private UUID orderId;

	@BeforeEach
	void setUp() {
		eventId = UUID.randomUUID();
		ticketTypeId = UUID.randomUUID();
		userId = UUID.randomUUID();
		ticketId = UUID.randomUUID();
		orderId = UUID.randomUUID();
	}

	@Test
	@DisplayName("POST /api/v1/published-event/{id}/ticket-types/{id} should purchase tickets successfully")
	void purchaseTicket_Success() throws Exception {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		TicketResponse ticketResponse = new TicketResponse(
				ticketId, TicketStatus.PURCHASED, "Event", "Location",
				Instant.now(), "VIP", UUID.randomUUID(), null, Instant.now());
		PurchaseTicketResponse response = new PurchaseTicketResponse(
				orderId,
				BigDecimal.valueOf(300.00),
				OrderStatus.PAID,
				List.of(ticketResponse, ticketResponse));

		when(ticketService.purchaseTicket(eventId, ticketTypeId, request, userId))
				.thenReturn(response);

		// Act & Assert
		mockMvc.perform(post("/api/v1/published-event/{publishedEventId}/ticket-types/{ticketTypeId}",
				eventId, ticketTypeId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString())
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.orderId").value(orderId.toString()))
				.andExpect(jsonPath("$.totalAmount").value(300.00))
				.andExpect(jsonPath("$.orderStatus").value("PAID"))
				.andExpect(jsonPath("$.tickets").isArray())
				.andExpect(jsonPath("$.tickets.length()").value(2));

		verify(ticketService).purchaseTicket(eventId, ticketTypeId, request, userId);
	}

	@Test
	@DisplayName("POST /api/v1/published-event/{id}/ticket-types/{id} should return 400 for invalid quantity")
	void purchaseTicket_InvalidQuantity() throws Exception {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(0);

		// Act & Assert
		mockMvc.perform(post("/api/v1/published-event/{publishedEventId}/ticket-types/{ticketTypeId}",
				eventId, ticketTypeId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString())
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"));

		verify(ticketService, never()).purchaseTicket(any(), any(), any(), any());
	}

	@Test
	@DisplayName("POST /api/v1/published-event/{id}/ticket-types/{id} should return 404 when event not found")
	void purchaseTicket_EventNotFound() throws Exception {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(2);
		when(ticketService.purchaseTicket(eventId, ticketTypeId, request, userId))
				.thenThrow(new EventNotFoundException(eventId));

		// Act & Assert
		mockMvc.perform(post("/api/v1/published-event/{publishedEventId}/ticket-types/{ticketTypeId}",
				eventId, ticketTypeId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString())
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Event not found"));

		verify(ticketService).purchaseTicket(eventId, ticketTypeId, request, userId);
	}

	@Test
	@DisplayName("POST /api/v1/published-event/{id}/ticket-types/{id} should return 400 when insufficient tickets")
	void purchaseTicket_InsufficientTickets() throws Exception {
		// Arrange
		PurchaseTicketRequest request = new PurchaseTicketRequest(100);
		when(ticketService.purchaseTicket(eventId, ticketTypeId, request, userId))
				.thenThrow(new InsufficientTicketsException(ticketTypeId, 100, 5));

		// Act & Assert
		mockMvc.perform(post("/api/v1/published-event/{publishedEventId}/ticket-types/{ticketTypeId}",
				eventId, ticketTypeId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString())
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Insufficient tickets available"));

		verify(ticketService).purchaseTicket(eventId, ticketTypeId, request, userId);
	}

	@Test
	@DisplayName("GET /api/v1/tickets should return list of user tickets")
	void listUserTickets_Success() throws Exception {
		// Arrange
		TicketResponse ticket1 = new TicketResponse(
				UUID.randomUUID(), TicketStatus.PURCHASED, "Event 1", "Location 1",
				Instant.now(), "VIP", UUID.randomUUID(), null, Instant.now());
		TicketResponse ticket2 = new TicketResponse(
				UUID.randomUUID(), TicketStatus.PURCHASED, "Event 2", "Location 2",
				Instant.now(), "Standard", UUID.randomUUID(), null, Instant.now());
		List<TicketResponse> tickets = List.of(ticket1, ticket2);

		when(ticketService.listUserTickets(userId)).thenReturn(tickets);

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].eventTitle").value("Event 1"))
				.andExpect(jsonPath("$[1].eventTitle").value("Event 2"));

		verify(ticketService).listUserTickets(userId);
	}

	@Test
	@DisplayName("GET /api/v1/tickets should return empty list when user has no tickets")
	void listUserTickets_EmptyList() throws Exception {
		// Arrange
		when(ticketService.listUserTickets(userId)).thenReturn(new ArrayList<>());

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());

		verify(ticketService).listUserTickets(userId);
	}

	@Test
	@DisplayName("GET /api/v1/tickets/{id} should return ticket details")
	void getTicket_Success() throws Exception {
		// Arrange
		TicketResponse ticketResponse = new TicketResponse(
				ticketId, TicketStatus.PURCHASED, "Spring Music Festival", "Central Park",
				Instant.now(), "VIP", UUID.randomUUID(), null, Instant.now());

		when(ticketService.getTicketById(ticketId, userId)).thenReturn(ticketResponse);

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets/{ticketId}", ticketId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(ticketId.toString()))
				.andExpect(jsonPath("$.eventTitle").value("Spring Music Festival"))
				.andExpect(jsonPath("$.ticketTypeName").value("VIP"))
				.andExpect(jsonPath("$.status").value("PURCHASED"));

		verify(ticketService).getTicketById(ticketId, userId);
	}

	@Test
	@DisplayName("GET /api/v1/tickets/{id} should return 404 when ticket not found")
	void getTicket_NotFound() throws Exception {
		// Arrange
		when(ticketService.getTicketById(ticketId, userId))
				.thenThrow(new TicketNotFoundException(ticketId));

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets/{ticketId}", ticketId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Ticket not found"));

		verify(ticketService).getTicketById(ticketId, userId);
	}

	@Test
	@DisplayName("GET /api/v1/tickets/{id} should return 403 when unauthorized")
	void getTicket_Unauthorized() throws Exception {
		// Arrange
		when(ticketService.getTicketById(ticketId, userId))
				.thenThrow(new UnauthorizedAccessException("You do not have access to this ticket"));

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets/{ticketId}", ticketId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Unauthorized access"));

		verify(ticketService).getTicketById(ticketId, userId);
	}

	@Test
	@DisplayName("GET /api/v1/tickets/{id}/qr-codes should return QR code")
	void getTicketQrCode_Success() throws Exception {
		// Arrange
		UUID qrCodeId = UUID.randomUUID();
		QrCodeResponse qrCodeResponse = new QrCodeResponse(
				qrCodeId,
				"https://api.event-platform.com/qr/" + qrCodeId,
				QrCodeStatusEnum.ACTIVE,
				Instant.now());

		when(ticketService.getTicketQrCode(ticketId, userId)).thenReturn(qrCodeResponse);

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets/{ticketId}/qr-codes", ticketId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(qrCodeId.toString()))
				.andExpect(jsonPath("$.codeData").value("https://api.event-platform.com/qr/" + qrCodeId))
				.andExpect(jsonPath("$.status").value("ACTIVE"));

		verify(ticketService).getTicketQrCode(ticketId, userId);
	}

	@Test
	@DisplayName("GET /api/v1/tickets/{id}/qr-codes should return 404 when ticket not found")
	void getTicketQrCode_NotFound() throws Exception {
		// Arrange
		when(ticketService.getTicketQrCode(ticketId, userId))
				.thenThrow(new TicketNotFoundException(ticketId));

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets/{ticketId}/qr-codes", ticketId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Ticket not found"));

		verify(ticketService).getTicketQrCode(ticketId, userId);
	}

	@Test
	@DisplayName("GET /api/v1/tickets/{id}/qr-codes should return 403 when unauthorized")
	void getTicketQrCode_Unauthorized() throws Exception {
		// Arrange
		when(ticketService.getTicketQrCode(ticketId, userId))
				.thenThrow(new UnauthorizedAccessException("You do not have access to this ticket"));

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets/{ticketId}/qr-codes", ticketId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Unauthorized access"));

		verify(ticketService).getTicketQrCode(ticketId, userId);
	}

	@Test
	@DisplayName("GET /api/v1/tickets/{id}/download should download ticket PDF successfully")
	void downloadTicketPdf_Success() throws Exception {
		// Arrange
		TicketResponse ticketResponse = new TicketResponse(
				ticketId, TicketStatus.PURCHASED, "Spring Music Festival", "Central Park",
				Instant.now(), "VIP", UUID.randomUUID(), null, Instant.now());
		QrCodeResponse qrCodeResponse = new QrCodeResponse(
				UUID.randomUUID(),
				"iVBORw0KGgoAAAANSUhEUgAAADIAAADICAYAAAC0K5ewAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAASeSURBVHic7doxbBNREAbgL5cE",
				QrCodeStatusEnum.ACTIVE,
				Instant.now());

		when(ticketService.getTicketById(ticketId, userId)).thenReturn(ticketResponse);
		when(ticketService.getTicketQrCode(ticketId, userId)).thenReturn(qrCodeResponse);
		when(pdfTicketService.generateTicketPdf(eq(ticketResponse), any(byte[].class)))
				.thenReturn(new java.io.ByteArrayOutputStream());

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets/{ticketId}/download", ticketId)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));

		verify(ticketService).getTicketById(ticketId, userId);
		verify(ticketService).getTicketQrCode(ticketId, userId);
		verify(pdfTicketService).generateTicketPdf(any(), any());
	}

	@Test
	@DisplayName("GET /api/v1/tickets/{id}/download should return 404 when ticket not found")
	void downloadTicketPdf_TicketNotFound() throws Exception {
		// Arrange
		when(ticketService.getTicketById(ticketId, userId))
				.thenThrow(new TicketNotFoundException(ticketId));

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets/{ticketId}/download", ticketId)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Ticket not found"));

		verify(ticketService).getTicketById(ticketId, userId);
		verify(ticketService, never()).getTicketQrCode(any(), any());
		verify(pdfTicketService, never()).generateTicketPdf(any(), any());
	}

	@Test
	@DisplayName("GET /api/v1/tickets/{id}/download should return 403 when unauthorized")
	void downloadTicketPdf_Unauthorized() throws Exception {
		// Arrange
		when(ticketService.getTicketById(ticketId, userId))
				.thenThrow(new UnauthorizedAccessException("You do not have access to this ticket"));

		// Act & Assert
		mockMvc.perform(get("/api/v1/tickets/{ticketId}/download", ticketId)
				.header("X-User-Id", userId.toString()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Unauthorized access"));

		verify(ticketService).getTicketById(ticketId, userId);
		verify(pdfTicketService, never()).generateTicketPdf(any(), any());
	}
}
