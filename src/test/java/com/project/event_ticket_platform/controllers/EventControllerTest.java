package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.services.EventService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventController.class)
@Import(EventControllerTest.TestConfig.class)
@TestPropertySource(properties = "app.jpa.auditing.enabled=false")
class EventControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EventService eventService;

	@Test
	void shouldCreateEvent() throws Exception {
		UUID eventId = UUID.randomUUID();
		UUID organizerId = UUID.randomUUID();
		Instant now = Instant.now();

		EventResponse response = new EventResponse(
			eventId,
			organizerId,
			"Launch Party",
			"Celebrate product launch",
			"Berlin",
			now,
			now.plusSeconds(3600),
			EventStatus.PUBLISHED,
			now,
			now
		);

		when(eventService.createEvent(any(CreateEventRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/v1/events")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"organizerId": "%s",
						"title": "Launch Party",
						"description": "Celebrate product launch",
						"location": "Berlin",
						"startTime": "2025-07-01T18:00:00Z",
						"endTime": "2025-07-01T20:00:00Z",
						"status": "PUBLISHED"
					}
					""".formatted(organizerId)))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "/api/v1/events/" + eventId))
			.andExpect(jsonPath("$.id").value(eventId.toString()))
			.andExpect(jsonPath("$.title").value("Launch Party"))
			.andExpect(jsonPath("$.status").value("PUBLISHED"));

		ArgumentCaptor<CreateEventRequest> captor = ArgumentCaptor.forClass(CreateEventRequest.class);
		verify(eventService).createEvent(captor.capture());
		CreateEventRequest captured = captor.getValue();
		assertThat(captured.organizerId()).isEqualTo(organizerId);
		assertThat(captured.title()).isEqualTo("Launch Party");
		assertThat(captured.status()).isEqualTo(EventStatus.PUBLISHED);
	}

	@Test
	void shouldListEvents() throws Exception {
		EventResponse response = new EventResponse(
			UUID.randomUUID(),
			UUID.randomUUID(),
			"Conference",
			"Industry conference",
			"Remote",
			Instant.parse("2025-09-01T09:00:00Z"),
			Instant.parse("2025-09-01T17:00:00Z"),
			EventStatus.PUBLISHED,
			Instant.now(),
			Instant.now()
		);

		when(eventService.getAllEvents()).thenReturn(List.of(response));

		mockMvc.perform(get("/api/v1/events"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].title").value("Conference"))
			.andExpect(jsonPath("$[0].status").value("PUBLISHED"));
	}

	@TestConfiguration
	static class TestConfig {

		@Bean
		EventService eventService() {
			return Mockito.mock(EventService.class);
		}
	}
}
