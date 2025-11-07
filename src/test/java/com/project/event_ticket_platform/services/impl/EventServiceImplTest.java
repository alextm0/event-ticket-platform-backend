package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.entities.UserRole;
import com.project.event_ticket_platform.exceptions.OrganizerNotFoundException;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

	@Mock
	private EventRepository eventRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private EventServiceImpl eventService;

	private User organizer;
	private UUID organizerId;

	@BeforeEach
	void setup() {
		organizerId = UUID.randomUUID();
		organizer = new User();
		organizer.setId(organizerId);
		organizer.setName("Organizer");
		organizer.setEmail("organizer@example.com");
		organizer.setRole(UserRole.ORGANIZER);
	}

	@Test
	void shouldCreateEventWithDefaultStatusWhenNotProvided() {
		CreateEventRequest request = new CreateEventRequest(
			organizerId,
			"Sample Event",
			"Description",
			"Online",
			Instant.parse("2025-06-01T10:00:00Z"),
			Instant.parse("2025-06-01T12:00:00Z"),
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
		CreateEventRequest request = new CreateEventRequest(
			organizerId,
			"Sample Event",
			null,
			null,
			null,
			null,
			EventStatus.PUBLISHED
		);

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

		when(eventRepository.findAll()).thenReturn(List.of(event));

		List<EventResponse> responses = eventService.getAllEvents();

		assertThat(responses).hasSize(1);
		EventResponse response = responses.get(0);
		assertThat(response.id()).isEqualTo(event.getId());
		assertThat(response.organizerId()).isEqualTo(organizerId);
		assertThat(response.status()).isEqualTo(EventStatus.PUBLISHED);
	}
}
