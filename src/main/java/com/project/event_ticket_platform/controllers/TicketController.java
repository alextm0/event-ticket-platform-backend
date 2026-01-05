package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.PurchaseTicketRequest;
import com.project.event_ticket_platform.dtos.PurchaseTicketResponse;
import com.project.event_ticket_platform.dtos.QrCodeResponse;
import com.project.event_ticket_platform.dtos.TicketResponse;
import com.project.event_ticket_platform.services.PdfTicketService;
import com.project.event_ticket_platform.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Tickets", description = "APIs for purchasing and managing tickets")
@RequiredArgsConstructor
public class TicketController {

	private final TicketService ticketService;
	private final PdfTicketService pdfTicketService;

	@Operation(summary = "Purchase tickets", description = "Purchase tickets for a published event. Returns order with generated QR codes.", responses = {
			@ApiResponse(responseCode = "201", description = "Tickets purchased successfully"),
			@ApiResponse(responseCode = "400", description = "Insufficient tickets available or event not published"),
			@ApiResponse(responseCode = "404", description = "Event or ticket type not found"),
			@ApiResponse(responseCode = "500", description = "QR code generation failed")
	})
	@PostMapping("/published-event/{publishedEventId}/ticket-types/{ticketTypeId}")
	public ResponseEntity<PurchaseTicketResponse> purchaseTickets(
			@Parameter(description = "Published event ID", required = true) @PathVariable("publishedEventId") UUID publishedEventId,
			@Parameter(description = "Ticket type ID", required = true) @PathVariable("ticketTypeId") UUID ticketTypeId,
			@Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") UUID userId,
			@Valid @RequestBody PurchaseTicketRequest request) {
		PurchaseTicketResponse response = ticketService.purchaseTicket(publishedEventId, ticketTypeId, request, userId);
		return ResponseEntity.status(201).body(response);
	}

	@Operation(summary = "List user tickets", description = "Retrieve all tickets for the authenticated user", responses = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved tickets")
	})
	@GetMapping("/tickets")
	public ResponseEntity<List<TicketResponse>> listUserTickets(
			@Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") UUID userId) {
		List<TicketResponse> tickets = ticketService.listUserTickets(userId);
		return ResponseEntity.ok(tickets);
	}

	@Operation(summary = "Get ticket by ID", description = "Retrieve a specific ticket by ID for the authenticated user", responses = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved ticket"),
			@ApiResponse(responseCode = "403", description = "Unauthorized access to ticket"),
			@ApiResponse(responseCode = "404", description = "Ticket not found")
	})
	@GetMapping("/tickets/{ticketId}")
	public ResponseEntity<TicketResponse> getTicketById(
			@Parameter(description = "Ticket ID", required = true) @PathVariable("ticketId") UUID ticketId,
			@Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") UUID userId) {
		TicketResponse ticket = ticketService.getTicketById(ticketId, userId);
		return ResponseEntity.ok(ticket);
	}

	@Operation(summary = "Get ticket QR code", description = "Retrieve the QR code for a specific ticket", responses = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved QR code"),
			@ApiResponse(responseCode = "403", description = "Unauthorized access to ticket"),
			@ApiResponse(responseCode = "404", description = "Ticket not found")
	})
	@GetMapping("/tickets/{ticketId}/qr-codes")
	public ResponseEntity<QrCodeResponse> getTicketQrCode(
			@Parameter(description = "Ticket ID", required = true) @PathVariable("ticketId") UUID ticketId,
			@Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") UUID userId) {
		QrCodeResponse qrCode = ticketService.getTicketQrCode(ticketId, userId);
		return ResponseEntity.ok(qrCode);
	}

	@Operation(summary = "Download ticket as PDF", description = "Download ticket as PDF with QR code for validation", responses = {
			@ApiResponse(responseCode = "200", description = "PDF ticket generated successfully"),
			@ApiResponse(responseCode = "403", description = "Unauthorized access to ticket"),
			@ApiResponse(responseCode = "404", description = "Ticket not found")
	})
	@GetMapping("/tickets/{ticketId}/download")
	public ResponseEntity<byte[]> downloadTicketPdf(
			@Parameter(description = "Ticket ID", required = true) @PathVariable("ticketId") UUID ticketId,
			@Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") UUID userId) {
		TicketResponse ticket = ticketService.getTicketById(ticketId, userId);
		QrCodeResponse qrCode = ticketService.getTicketQrCode(ticketId, userId);
		byte[] qrCodeImage = java.util.Base64.getDecoder().decode(qrCode.codeData());
		ByteArrayOutputStream pdf = pdfTicketService.generateTicketPdf(ticket, qrCodeImage);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", "ticket-" + ticketId + ".pdf");
		headers.setContentLength(pdf.size());

		return ResponseEntity.ok()
				.headers(headers)
				.body(pdf.toByteArray());
	}
}
