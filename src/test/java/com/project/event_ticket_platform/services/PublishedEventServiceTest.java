package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.PublishedEventResponse;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.EventNotPublishedException;
import com.project.event_ticket_platform.mappers.PublishedEventMapper;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.services.impl.PublishedEventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublishedEventServiceTest {

	@Mock
	private EventRepository eventRepository;

	@Mock
	private PublishedEventMapper publishedEventMapper;

	@InjectMocks
	private PublishedEventServiceImpl publishedEventService;

	private Event publishedEvent;
	private PublishedEventResponse publishedEventResponse;
	private UUID eventId;
	private User organizer;

	@BeforeEach
	void setUp() {
		eventId = UUID.randomUUID();

		organizer = new User();
		organizer.setId(UUID.randomUUID());
		organizer.setName("John Doe");
		organizer.setEmail("john@example.com");

		publishedEvent = new Event();
		publishedEvent.setId(eventId);
		publishedEvent.setTitle("Spring Music Festival");
		publishedEvent.setDescription("Amazing music festival");
		publishedEvent.setLocation("Central Park");
		publishedEvent.setStartTime(Instant.now().plusSeconds(86400));
		publishedEvent.setEndTime(Instant.now().plusSeconds(172800));
		publishedEvent.setStatus(EventStatus.PUBLISHED);
		publishedEvent.setOrganizer(organizer);
		publishedEvent.setTicketTypes(new ArrayList<>());

		publishedEventResponse = new PublishedEventResponse(
			eventId,
			"Spring Music Festival",
			"Amazing music festival",
			"Central Park",
			publishedEvent.getStartTime(),
			publishedEvent.getEndTime(),
			EventStatus.PUBLISHED,
			"John Doe",
			new ArrayList<>()
		);
	}

	@Test
	@DisplayName("Should return paginated list of published events")
	void listPublishedEvents_Success() {
		// Arrange
		Pageable pageable = PageRequest.of(0, 20);
		Page<Event> eventPage = new PageImpl<>(List.of(publishedEvent), pageable, 1);
		when(eventRepository.findAllByStatus(EventStatus.PUBLISHED, pageable)).thenReturn(eventPage);
		when(publishedEventMapper.toResponse(publishedEvent)).thenReturn(publishedEventResponse);

		// Act
		Page<PublishedEventResponse> result = publishedEventService.listPublishedEvents(0, 20);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("Spring Music Festival", result.getContent().get(0).title());
		verify(eventRepository).findAllByStatus(EventStatus.PUBLISHED, pageable);
		verify(publishedEventMapper).toResponse(publishedEvent);
	}

	@Test
	@DisplayName("Should return empty page when no published events exist")
	void listPublishedEvents_EmptyList() {
		// Arrange
		Pageable pageable = PageRequest.of(0, 20);
		Page<Event> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
		when(eventRepository.findAllByStatus(EventStatus.PUBLISHED, pageable)).thenReturn(emptyPage);

		// Act
		Page<PublishedEventResponse> result = publishedEventService.listPublishedEvents(0, 20);

		// Assert
		assertNotNull(result);
		assertTrue(result.isEmpty());
		assertEquals(0, result.getTotalElements());
		verify(eventRepository).findAllByStatus(EventStatus.PUBLISHED, pageable);
		verify(publishedEventMapper, never()).toResponse(any());
	}

	@Test
	@DisplayName("Should return published event by ID")
	void getPublishedEventById_Success() {
		// Arrange
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));
		when(publishedEventMapper.toResponse(publishedEvent)).thenReturn(publishedEventResponse);

		// Act
		PublishedEventResponse result = publishedEventService.getPublishedEventById(eventId);

		// Assert
		assertNotNull(result);
		assertEquals(eventId, result.id());
		assertEquals("Spring Music Festival", result.title());
		assertEquals(EventStatus.PUBLISHED, result.status());
		verify(eventRepository).findById(eventId);
		verify(publishedEventMapper).toResponse(publishedEvent);
	}

	@Test
	@DisplayName("Should throw EventNotFoundException when event does not exist")
	void getPublishedEventById_EventNotFound() {
		// Arrange
		when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EventNotFoundException.class, () -> {
			publishedEventService.getPublishedEventById(eventId);
		});
		verify(eventRepository).findById(eventId);
		verify(publishedEventMapper, never()).toResponse(any());
	}

	@Test
	@DisplayName("Should throw EventNotPublishedException when event is not published")
	void getPublishedEventById_EventNotPublished() {
		// Arrange
		publishedEvent.setStatus(EventStatus.DRAFT);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));

		// Act & Assert
		assertThrows(EventNotPublishedException.class, () -> {
			publishedEventService.getPublishedEventById(eventId);
		});
		verify(eventRepository).findById(eventId);
		verify(publishedEventMapper, never()).toResponse(any());
	}

	@Test
	@DisplayName("Should throw EventNotPublishedException when event is cancelled")
	void getPublishedEventById_EventCancelled() {
		// Arrange
		publishedEvent.setStatus(EventStatus.CANCELLED);
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(publishedEvent));

		// Act & Assert
		assertThrows(EventNotPublishedException.class, () -> {
			publishedEventService.getPublishedEventById(eventId);
		});
		verify(eventRepository).findById(eventId);
		verify(publishedEventMapper, never()).toResponse(any());
	}
}

