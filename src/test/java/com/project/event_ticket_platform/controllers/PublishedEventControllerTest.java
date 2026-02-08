package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.PublishedEventResponse;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.EventNotPublishedException;
import com.project.event_ticket_platform.services.JwtService;
import com.project.event_ticket_platform.services.PublishedEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PublishedEventController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class PublishedEventControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PublishedEventService publishedEventService;

	@MockitoBean
	private JwtService jwtService;

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
				new ArrayList<>());
	}

	@Test
	@DisplayName("GET /api/v1/published-events should return paginated list of published events")
	void listPublishedEvents_Success() throws Exception {
		// Arrange
		Page<PublishedEventResponse> eventPage = new PageImpl<>(
				List.of(publishedEventResponse),
				PageRequest.of(0, 20),
				1);
		when(publishedEventService.listPublishedEvents(0, 20)).thenReturn(eventPage);

		// Act & Assert
		mockMvc.perform(get("/api/v1/published-events")
				.param("page", "0")
				.param("size", "20")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content[0].id").value(eventId.toString()))
				.andExpect(jsonPath("$.content[0].title").value("Spring Music Festival"))
				.andExpect(jsonPath("$.content[0].status").value("PUBLISHED"))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.size").value(20));

		verify(publishedEventService).listPublishedEvents(0, 20);
	}

	@Test
	@DisplayName("GET /api/v1/published-events should return empty page when no events")
	void listPublishedEvents_EmptyList() throws Exception {
		// Arrange
		Page<PublishedEventResponse> emptyPage = new PageImpl<>(
				new ArrayList<>(),
				PageRequest.of(0, 20),
				0);
		when(publishedEventService.listPublishedEvents(0, 20)).thenReturn(emptyPage);

		// Act & Assert
		mockMvc.perform(get("/api/v1/published-events")
				.param("page", "0")
				.param("size", "20")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.totalElements").value(0));

		verify(publishedEventService).listPublishedEvents(0, 20);
	}

	@Test
	@DisplayName("GET /api/v1/published-events should use default pagination when params not provided")
	void listPublishedEvents_DefaultPagination() throws Exception {
		// Arrange
		Page<PublishedEventResponse> eventPage = new PageImpl<>(
				List.of(publishedEventResponse),
				PageRequest.of(0, 20),
				1);
		when(publishedEventService.listPublishedEvents(0, 20)).thenReturn(eventPage);

		// Act & Assert
		mockMvc.perform(get("/api/v1/published-events")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.size").value(20));

		verify(publishedEventService).listPublishedEvents(0, 20);
	}

	@Test
	@DisplayName("GET /api/v1/published-event/{id} should return event details")
	void getPublishedEvent_Success() throws Exception {
		// Arrange
		when(publishedEventService.getPublishedEventById(eventId)).thenReturn(publishedEventResponse);

		// Act & Assert
		mockMvc.perform(get("/api/v1/published-event/{publishedEventId}", eventId)
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
		mockMvc.perform(get("/api/v1/published-event/{publishedEventId}", eventId)
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
		mockMvc.perform(get("/api/v1/published-event/{publishedEventId}", eventId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Event not published"));

		verify(publishedEventService).getPublishedEventById(eventId);
	}
}
