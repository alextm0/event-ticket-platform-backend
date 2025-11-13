package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.CreateTicketTypeRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.dtos.TicketTypeResponse;
import com.project.event_ticket_platform.dtos.UpdateEventRequest;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.entities.TicketType;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.entities.UserRole;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.EventValidationException;
import com.project.event_ticket_platform.exceptions.OrganizerNotFoundException;
import com.project.event_ticket_platform.mappers.EventMapper;
import com.project.event_ticket_platform.mappers.TicketMapper;
import com.project.event_ticket_platform.mappers.TicketTypeMapper;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.repositories.TicketRepository;
import com.project.event_ticket_platform.repositories.TicketTypeRepository;
import com.project.event_ticket_platform.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

	@Mock
	private EventRepository eventRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketTypeRepository ticketTypeRepository;

	private EventServiceImpl eventService;

	private User organizer;
	private UUID organizerId;

	@BeforeEach
	void setup() {
		EventMapper eventMapper = Mappers.getMapper(EventMapper.class);
		TicketMapper ticketMapper = Mappers.getMapper(TicketMapper.class);
		TicketTypeMapper ticketTypeMapper = Mappers.getMapper(TicketTypeMapper.class);
		eventService = new EventServiceImpl(eventRepository, userRepository, ticketRepository, ticketTypeRepository, eventMapper, ticketMapper, ticketTypeMapper);

		organizerId = UUID.randomUUID();
		organizer = new User();
		organizer.setId(organizerId);
		organizer.setName("Organizer");
		organizer.setEmail("organizer@example.com");
		organizer.setRole(UserRole.ORGANIZER);
	}

	@Test
	void shouldCreateEventWithDefaultStatusWhenNotProvided() {
		Instant start = Instant.now().plusSeconds(3600);
		Instant end = start.plusSeconds(3600);

		CreateEventRequest request = new CreateEventRequest(
			organizerId,
			"Sample Event",
			"Description",
			"Online",
			start,
			end,
			null
		);

		when(userRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
		when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
			Event event = invocation.getArgument(0);
			event.setId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
			event.setCreatedAt(Instant.parse("2025-05-01T10:00:00Z"));
			event.setUpdatedAt(Instant.parse("2025-05-01T10:00:00Z"));
			return event;
		});

		EventResponse response = eventService.createEvent(request);

		assertThat(response.title()).isEqualTo("Sample Event");
		assertThat(response.status()).isEqualTo(EventStatus.DRAFT);
		assertThat(response.organizerId()).isEqualTo(organizerId);

		ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
		verify(eventRepository).save(captor.capture());
		Event persisted = captor.getValue();
		assertThat(persisted.getOrganizer()).isEqualTo(organizer);
		assertThat(persisted.getStatus()).isEqualTo(EventStatus.DRAFT);
		assertThat(persisted.getTitle()).isEqualTo("Sample Event");
	}

	@Test
	void shouldThrowWhenOrganizerNotFound() {
		CreateEventRequest request = validRequestBuilder()
			.status(EventStatus.PUBLISHED)
			.build();

		when(userRepository.findById(organizerId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> eventService.createEvent(request))
			.isInstanceOf(OrganizerNotFoundException.class);
	}

	@Test
	void shouldListEvents() {
		Event event = new Event();
		event.setId(UUID.fromString("ffffffff-1111-2222-3333-444444444444"));
		event.setOrganizer(organizer);
		event.setTitle("Listed Event");
		event.setStatus(EventStatus.PUBLISHED);
		event.setCreatedAt(Instant.parse("2025-05-02T10:00:00Z"));
		event.setUpdatedAt(Instant.parse("2025-05-02T11:00:00Z"));

		when(eventRepository.findAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(event)));

		Page<EventResponse> responses = eventService.getAllEvents(Pageable.unpaged());

		assertThat(responses.getTotalElements()).isEqualTo(1);
		EventResponse response = responses.getContent().get(0);
		assertThat(response.id()).isEqualTo(event.getId());
		assertThat(response.organizerId()).isEqualTo(organizerId);
		assertThat(response.status()).isEqualTo(EventStatus.PUBLISHED);
	}

	@Test
	void shouldRejectEventsWhereEndIsBeforeStart() {
		Instant start = Instant.now().plusSeconds(7200);
		Instant end = start.minusSeconds(600);

		CreateEventRequest request = new CreateEventRequest(
			organizerId,
			"Invalid Event",
			"Desc",
			"Remote",
			start,
			end,
			EventStatus.DRAFT
		);

		when(userRepository.findById(organizerId)).thenReturn(Optional.of(organizer));

		assertThatThrownBy(() -> eventService.createEvent(request))
			.isInstanceOf(EventValidationException.class);
	}

	@Test
	void shouldPublishDraftEvent() {
		Event event = new Event();
		event.setId(UUID.randomUUID());
		event.setStatus(EventStatus.DRAFT);
		event.setOrganizer(organizer);

		when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
		when(eventRepository.save(event)).thenReturn(event);

		Instant start = Instant.now().plusSeconds(3600);
		Instant end = start.plusSeconds(3600);
		UpdateEventRequest request = new UpdateEventRequest(
			"Launch Party",
			"Desc",
			"Berlin",
			start,
			end,
			EventStatus.PUBLISHED
		);

		EventResponse response = eventService.updateEvent(event.getId(), request);

		assertThat(response.status()).isEqualTo(EventStatus.PUBLISHED);
		assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
		verify(eventRepository).save(event);
	}

	@Test
	void shouldFailForUnsupportedStatusTransition() {
		Event event = new Event();
		event.setId(UUID.randomUUID());
		event.setStatus(EventStatus.CANCELLED);
		event.setOrganizer(organizer);

		when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

		UpdateEventRequest request = new UpdateEventRequest(
			"Launch Party",
			"Desc",
			"Berlin",
			Instant.now().plusSeconds(3600),
			Instant.now().plusSeconds(7200),
			EventStatus.PUBLISHED
		);

		assertThatThrownBy(() -> eventService.updateEvent(event.getId(), request))
			.isInstanceOf(EventValidationException.class);
	}

	@Test
	void shouldAllowNoOpStatusUpdate() {
		Event event = new Event();
		event.setId(UUID.randomUUID());
		event.setStatus(EventStatus.PUBLISHED);
		event.setOrganizer(organizer);

		when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
		when(eventRepository.save(event)).thenReturn(event);

		UpdateEventRequest request = new UpdateEventRequest(
			"Launch Party",
			"Desc",
			"Berlin",
			Instant.now().plusSeconds(3600),
			Instant.now().plusSeconds(7200),
			EventStatus.PUBLISHED
		);

		eventService.updateEvent(event.getId(), request);

		verify(eventRepository).save(event);
	}

	@Test
	void shouldAllowPublishedToCancelledTransition() {
		Event event = new Event();
		event.setId(UUID.randomUUID());
		event.setStatus(EventStatus.PUBLISHED);
		event.setOrganizer(organizer);

		when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
		when(eventRepository.save(event)).thenReturn(event);

		UpdateEventRequest request = new UpdateEventRequest(
			"Launch Party",
			"Desc",
			"Berlin",
			Instant.now().plusSeconds(3600),
			Instant.now().plusSeconds(7200),
			EventStatus.CANCELLED
		);

		EventResponse response = eventService.updateEvent(event.getId(), request);

		assertThat(response.status()).isEqualTo(EventStatus.CANCELLED);
		verify(eventRepository).save(event);
	}

	@Test
	void shouldThrowWhenEventNotFoundOnPublish() {
		UUID eventId = UUID.randomUUID();
		when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

		UpdateEventRequest request = new UpdateEventRequest(
			"Launch Party",
			"Desc",
			"Berlin",
			Instant.now().plusSeconds(3600),
			Instant.now().plusSeconds(7200),
			EventStatus.PUBLISHED
		);

		assertThatThrownBy(() -> eventService.updateEvent(eventId, request))
			.isInstanceOf(EventNotFoundException.class);
	}

	@Test
	void shouldDeleteEvent() {
		UUID eventId = UUID.randomUUID();
		Event event = new Event();
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
		doNothing().when(eventRepository).delete(event);

		eventService.deleteEvent(eventId);

		verify(eventRepository).delete(event);
	}

	@Test
	void shouldCreateTicketTypeForEvent() {
		UUID eventId = UUID.randomUUID();
		Event event = new Event();
		event.setId(eventId);

		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
		when(ticketTypeRepository.save(any(TicketType.class))).thenAnswer(invocation -> {
			TicketType ticketType = invocation.getArgument(0);
			ticketType.setId(UUID.fromString("bbbbbbbb-1111-2222-3333-444444444444"));
			return ticketType;
		});

		CreateTicketTypeRequest request = new CreateTicketTypeRequest(
				"VIP",
				"Front row access",
				new BigDecimal("120.00"),
				80,
				true
		);

		TicketTypeResponse response = eventService.createTicketTypeForEvent(eventId, request);

		assertThat(response.id()).isEqualTo(UUID.fromString("bbbbbbbb-1111-2222-3333-444444444444"));
		assertThat(response.name()).isEqualTo("VIP");
		assertThat(response.price()).isEqualByComparingTo("120.00");
		assertThat(response.totalQuantity()).isEqualTo(80);
		assertThat(response.availableQuantity()).isEqualTo(80);
		assertThat(response.active()).isTrue();

		ArgumentCaptor<TicketType> captor = ArgumentCaptor.forClass(TicketType.class);
		verify(ticketTypeRepository).save(captor.capture());
		TicketType saved = captor.getValue();
		assertThat(saved.getEvent()).isEqualTo(event);
		assertThat(saved.getSoldCount()).isEqualTo(0);
		assertThat(saved.isActive()).isTrue();
	}

	@Test
	void shouldThrowWhenDeletingMissingEvent() {
		UUID eventId = UUID.randomUUID();
		when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> eventService.deleteEvent(eventId))
			.isInstanceOf(EventNotFoundException.class);

		verify(eventRepository, never()).delete(any());
	}

	private ValidRequestBuilder validRequestBuilder() {
		return new ValidRequestBuilder();
	}

	private class ValidRequestBuilder {
		private String title = "Sample Event";
		private String description = "Description";
		private String location = "Online";
		private Instant baseTime = Instant.now();
		private Instant start = baseTime.plusSeconds(3600);
		private Instant end = baseTime.plusSeconds(7200);
		private EventStatus status = null;

		ValidRequestBuilder status(EventStatus status) {
			this.status = status;
			return this;
		}

		CreateEventRequest build() {
			return new CreateEventRequest(
				organizerId,
				title,
				description,
				location,
				start,
				end,
				status
			);
		}
	}
}
