package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.TicketValidationResponse;
import com.project.event_ticket_platform.dtos.ValidateTicketRequest;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.QrCode;
import com.project.event_ticket_platform.entities.Ticket;
import com.project.event_ticket_platform.entities.TicketStatus;
import com.project.event_ticket_platform.entities.TicketType;
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
import com.project.event_ticket_platform.services.impl.TicketValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketValidationServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketValidationRepository ticketValidationRepository;

	@Mock
	private TicketValidationMapper ticketValidationMapper;

	@Mock
	private EventRepository eventRepository;

	@Mock
	private EventStaffRepository eventStaffRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private TicketValidationServiceImpl ticketValidationService;

	private UUID eventId;
	private UUID staffId;
	private UUID qrCodeId;
	private Event event;
	private User staffUser;
	private Ticket ticket;
	private ValidateTicketRequest request;

	@BeforeEach
	void setUp() {
		eventId = UUID.randomUUID();
		staffId = UUID.randomUUID();
		qrCodeId = UUID.randomUUID();
		request = new ValidateTicketRequest(qrCodeId);

		event = new Event();
		event.setId(eventId);
		event.setTitle("Conference 2025");

		staffUser = new User();
		staffUser.setId(staffId);
		staffUser.setRole(UserRole.STAFF);

		TicketType ticketType = new TicketType();
		ticketType.setEvent(event);

		QrCode qrCode = new QrCode();
		qrCode.setId(qrCodeId);

		ticket = new Ticket();
		ticket.setId(UUID.randomUUID());
		ticket.setStatus(TicketStatus.PURCHASED);
		ticket.setTicketType(ticketType);
		ticket.setQrCode(qrCode);
	}

	@Test
	@DisplayName("Should validate ticket successfully and mark it as checked in")
	void validateTicket_successful() {
		mockStaffAccess();
		when(ticketRepository.findByQrCodeIdWithEventForUpdate(qrCodeId)).thenReturn(Optional.of(ticket));
		when(ticketValidationRepository.save(any(TicketValidation.class))).thenAnswer(invocation -> {
			TicketValidation validation = invocation.getArgument(0);
			validation.setId(UUID.randomUUID());
			return validation;
		});
		when(ticketValidationMapper.toResponse(any(TicketValidation.class))).thenAnswer(invocation -> mapToResponse(invocation.getArgument(0)));

		TicketValidationResponse response = ticketValidationService.validateTicket(eventId, staffId, request);

		assertNotNull(response);
		assertEquals(qrCodeId, response.qrCodeId());
		assertEquals(TicketValidationEnum.VALID, response.validationStatus());
		assertEquals(TicketStatus.CHECKED_IN, ticket.getStatus());

		verify(ticketRepository).save(ticket);

		ArgumentCaptor<TicketValidation> captor = ArgumentCaptor.forClass(TicketValidation.class);
		verify(ticketValidationRepository).save(captor.capture());

		TicketValidation persisted = captor.getValue();
		assertEquals(TicketValidationEnum.VALID, persisted.getStatus());
		assertEquals(TicketValidationMethodEnum.QR_SCAN, persisted.getValidationMethod());
		assertNotNull(persisted.getValidationDateTime());
	}

	@Test
	@DisplayName("Should record invalid validation when ticket already checked in")
	void validateTicket_ticketAlreadyCheckedIn() {
		ticket.setStatus(TicketStatus.CHECKED_IN);
		ticket.setCheckedInAt(Instant.now().minusSeconds(60));

		mockStaffAccess();
		when(ticketRepository.findByQrCodeIdWithEventForUpdate(qrCodeId)).thenReturn(Optional.of(ticket));
		when(ticketValidationRepository.save(any(TicketValidation.class))).thenAnswer(invocation -> {
			TicketValidation validation = invocation.getArgument(0);
			validation.setId(UUID.randomUUID());
			return validation;
		});
		when(ticketValidationMapper.toResponse(any(TicketValidation.class))).thenAnswer(invocation -> mapToResponse(invocation.getArgument(0)));

		TicketValidationResponse response = ticketValidationService.validateTicket(eventId, staffId, request);

		assertNotNull(response);
		assertEquals(TicketValidationEnum.INVALID, response.validationStatus());
		verify(ticketRepository, never()).save(any(Ticket.class));
	}

	@Test
	@DisplayName("Should reject validation when ticket does not belong to event")
	void validateTicket_ticketBelongsToAnotherEvent() {
		Event differentEvent = new Event();
		differentEvent.setId(UUID.randomUUID());
		ticket.getTicketType().setEvent(differentEvent);

		mockStaffAccess();
		when(ticketRepository.findByQrCodeIdWithEventForUpdate(qrCodeId)).thenReturn(Optional.of(ticket));

		assertThrows(UnauthorizedAccessException.class, () ->
			ticketValidationService.validateTicket(eventId, staffId, request)
		);
		verify(ticketValidationRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should throw when QR code id is missing")
	void validateTicket_missingQrCode() {
		assertThrows(IllegalArgumentException.class, () ->
			ticketValidationService.validateTicket(eventId, staffId, new ValidateTicketRequest(null))
		);
		verifyNoInteractions(eventRepository);
	}

	@Test
	@DisplayName("Should throw when event is not found")
	void validateTicket_eventNotFound() {
		when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

		assertThrows(EventNotFoundException.class, () ->
			ticketValidationService.validateTicket(eventId, staffId, request)
		);
		verify(eventRepository).findById(eventId);
		verifyNoInteractions(userRepository);
	}

	@Test
	@DisplayName("Should throw when staff user cannot be located")
	void validateTicket_staffNotFound() {
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
		when(userRepository.findById(staffId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () ->
			ticketValidationService.validateTicket(eventId, staffId, request)
		);
		verify(userRepository).findById(staffId);
	}

	@Test
	@DisplayName("Should block validation when user is not staff")
	void validateTicket_userNotStaff() {
		staffUser.setRole(UserRole.ATTENDEE);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
		when(userRepository.findById(staffId)).thenReturn(Optional.of(staffUser));

		assertThrows(UnauthorizedAccessException.class, () ->
			ticketValidationService.validateTicket(eventId, staffId, request)
		);
		verify(eventStaffRepository, never()).existsByEventIdAndStaffId(any(), any());
	}

	@Test
	@DisplayName("Should block validation when staff is not assigned to event")
	void validateTicket_staffNotAssigned() {
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
		when(userRepository.findById(staffId)).thenReturn(Optional.of(staffUser));
		when(eventStaffRepository.existsByEventIdAndStaffId(eventId, staffId)).thenReturn(false);

		assertThrows(UnauthorizedAccessException.class, () ->
			ticketValidationService.validateTicket(eventId, staffId, request)
		);
		verify(eventStaffRepository).existsByEventIdAndStaffId(eventId, staffId);
	}

	@Test
	@DisplayName("Should raise when QR code cannot be resolved to a ticket")
	void validateTicket_qrCodeNotFound() {
		mockStaffAccess();
		when(ticketRepository.findByQrCodeIdWithEventForUpdate(qrCodeId)).thenReturn(Optional.empty());

		assertThrows(QrCodeNotFoundException.class, () ->
			ticketValidationService.validateTicket(eventId, staffId, request)
		);
		verify(ticketRepository).findByQrCodeIdWithEventForUpdate(qrCodeId);
	}

	@Test
	@DisplayName("Should list validations for authorized staff")
	void listValidations_success() {
		mockStaffAccess();

		TicketValidation validation = new TicketValidation();
		validation.setId(UUID.randomUUID());
		validation.setTicket(ticket);
		validation.setStatus(TicketValidationEnum.VALID);
		validation.setValidationMethod(TicketValidationMethodEnum.QR_SCAN);
		validation.setValidationDateTime(Instant.now());

		when(ticketValidationRepository.findAllByEventIdWithDetails(eventId)).thenReturn(List.of(validation));
		TicketValidationResponse expectedResponse = mapToResponse(validation);
		when(ticketValidationMapper.toResponse(validation)).thenReturn(expectedResponse);

		List<TicketValidationResponse> responses = ticketValidationService.listValidations(eventId, staffId);

		assertEquals(1, responses.size());
		assertSame(expectedResponse, responses.get(0));
		verify(ticketValidationRepository).findAllByEventIdWithDetails(eventId);
	}

	private void mockStaffAccess() {
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
		when(userRepository.findById(staffId)).thenReturn(Optional.of(staffUser));
		when(eventStaffRepository.existsByEventIdAndStaffId(eventId, staffId)).thenReturn(true);
	}

	private TicketValidationResponse mapToResponse(TicketValidation validation) {
		return new TicketValidationResponse(
			validation.getId(),
			validation.getTicket().getTicketType().getEvent().getId(),
			validation.getTicket().getTicketType().getEvent().getTitle(),
			validation.getTicket().getId(),
			validation.getTicket().getQrCode().getId(),
			validation.getTicket().getStatus(),
			validation.getStatus(),
			validation.getValidationMethod(),
			validation.getValidationDateTime()
		);
	}
}
