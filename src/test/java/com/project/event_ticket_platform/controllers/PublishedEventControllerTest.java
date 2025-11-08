package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.PublishedEventResponse;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.EventNotPublishedException;
import com.project.event_ticket_platform.services.PublishedEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublishedEventController.class)
class PublishedEventControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PublishedEventService publishedEventService;

	private PublishedEventResponse publishedEventResponse;
	private UUID eventId;

	@BeforeEach
	void setUp() {
		eventId = UUID.randomUUID();
		publishedEventResponse = new PublishedEventResponse(
			eventId,
			"Spring Music Festival",
			"Amazing music festival",
			"Central Park",
			Instant.now().plusSeconds(86400),
			Instant.now().plusSeconds(172800),
			EventStatus.PUBLISHED,
			"John Doe",
			new ArrayList<>()
		);
	}

	@Test
	@DisplayName("GET /api/v1/published-events should return list of published events")
	void searchPublishedEvents_Success() throws Exception {
		// Arrange
		List<PublishedEventResponse> events = List.of(publishedEventResponse);
		when(publishedEventService.searchPublishedEvents()).thenReturn(events);

		// Act & Assert
		mockMvc.perform(get("/api/v1/published-events")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].id").value(eventId.toString()))
			.andExpect(jsonPath("$[0].title").value("Spring Music Festival"))
			.andExpect(jsonPath("$[0].status").value("PUBLISHED"));

		verify(publishedEventService).searchPublishedEvents();
	}

	@Test
	@DisplayName("GET /api/v1/published-events should return empty list when no events")
	void searchPublishedEvents_EmptyList() throws Exception {
		// Arrange
		when(publishedEventService.searchPublishedEvents()).thenReturn(new ArrayList<>());

		// Act & Assert
		mockMvc.perform(get("/api/v1/published-events")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isEmpty());

		verify(publishedEventService).searchPublishedEvents();
	}

	@Test
	@DisplayName("GET /api/v1/published-event/{id} should return event details")
	void getPublishedEvent_Success() throws Exception {
		// Arrange
		when(publishedEventService.getPublishedEventById(eventId)).thenReturn(publishedEventResponse);

		// Act & Assert
		mockMvc.perform(get("/api/v1/published-event/{published_event_id}", eventId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(eventId.toString()))
			.andExpect(jsonPath("$.title").value("Spring Music Festival"))
			.andExpect(jsonPath("$.location").value("Central Park"))
			.andExpect(jsonPath("$.status").value("PUBLISHED"))
			.andExpect(jsonPath("$.organizerName").value("John Doe"));

		verify(publishedEventService).getPublishedEventById(eventId);
	}

	@Test
	@DisplayName("GET /api/v1/published-event/{id} should return 404 when event not found")
	void getPublishedEvent_NotFound() throws Exception {
		// Arrange
		when(publishedEventService.getPublishedEventById(eventId))
			.thenThrow(new EventNotFoundException(eventId));

		// Act & Assert
		mockMvc.perform(get("/api/v1/published-event/{published_event_id}", eventId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.title").value("Event not found"));

		verify(publishedEventService).getPublishedEventById(eventId);
	}

	@Test
	@DisplayName("GET /api/v1/published-event/{id} should return 400 when event not published")
	void getPublishedEvent_NotPublished() throws Exception {
		// Arrange
		when(publishedEventService.getPublishedEventById(eventId))
			.thenThrow(new EventNotPublishedException(eventId));

		// Act & Assert
		mockMvc.perform(get("/api/v1/published-event/{published_event_id}", eventId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Event not published"));

		verify(publishedEventService).getPublishedEventById(eventId);
	}
}

