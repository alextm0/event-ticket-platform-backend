package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.dtos.EventTicketSaleResponse;
import com.project.event_ticket_platform.dtos.TicketTypeResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
		Instant start = Instant.now().plusSeconds(3600);
		Instant end = start.plusSeconds(3600);

		Instant now = Instant.now();

		EventResponse response = new EventResponse(
			eventId,
			organizerId,
			"Launch Party",
			"Celebrate product launch",
			"Berlin",
			start,
			end,
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
						"startTime": "%s",
						"endTime": "%s",
						"status": "PUBLISHED"
					}
					""".formatted(organizerId, start.toString(), end.toString())))
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

		Page<EventResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

		when(eventService.getAllEvents(any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/v1/events").param("page", "0").param("size", "20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].title").value("Conference"))
			.andExpect(jsonPath("$.content[0].status").value("PUBLISHED"))
			.andExpect(jsonPath("$.size").value(20))
			.andExpect(jsonPath("$.number").value(0))
			.andExpect(jsonPath("$.totalElements").value(1));
	}

	@TestConfiguration
	static class TestConfig {

		@Bean
		EventService eventService() {
			return Mockito.mock(EventService.class);
		}
	}

	@Test
	void shouldPublishEvent() throws Exception {
		UUID eventId = UUID.randomUUID();
		EventResponse response = new EventResponse(
			eventId,
			UUID.randomUUID(),
			"Launch Party",
			"Desc",
			"Berlin",
			Instant.now().plusSeconds(3600),
			Instant.now().plusSeconds(7200),
			EventStatus.PUBLISHED,
			Instant.now(),
			Instant.now()
		);

		when(eventService.updateEvent(any(UUID.class), any())).thenReturn(response);

		mockMvc.perform(put("/api/v1/events/{eventId}", eventId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"title": "Launch Party",
						"description": "Desc",
						"location": "Berlin",
						"startTime": "%s",
						"endTime": "%s",
						"status": "PUBLISHED"
					}
					""".formatted(response.startTime().toString(), response.endTime().toString())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("PUBLISHED"));

		verify(eventService).updateEvent(any(UUID.class), any());
	}

	@Test
	void shouldDeleteEvent() throws Exception {
		UUID eventId = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/events/{eventId}", eventId))
			.andExpect(status().isNoContent());

		verify(eventService).deleteEvent(eventId);
	}

	@Test
	void shouldGetTicketSalesForEvent() throws Exception {
		UUID eventId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		Instant purchaseDate = Instant.now();
		EventTicketSaleResponse ticketSale = new EventTicketSaleResponse(
				ticketId,
				eventId,
				UUID.randomUUID(),
				"VIP",
				UUID.randomUUID(),
				"John Doe",
				1,
				purchaseDate
		);

		Page<EventTicketSaleResponse> page = new PageImpl<>(List.of(ticketSale), PageRequest.of(0, 10), 1);

		when(eventService.getTicketSalesForEvent(any(UUID.class), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/v1/events/{eventId}/tickets", eventId)
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(ticketSale.id().toString()))
				.andExpect(jsonPath("$.content[0].ticketTypeName").value("VIP"))
				.andExpect(jsonPath("$.content[0].buyerName").value("John Doe"))
				.andExpect(jsonPath("$.content[0].quantity").value(1))
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.totalElements").value(1));

		verify(eventService).getTicketSalesForEvent(any(UUID.class), any(Pageable.class));
	}

	@Test
	void shouldGetTicketSaleForEvent() throws Exception {
		UUID eventId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		Instant purchaseDate = Instant.now();
		EventTicketSaleResponse ticketSale = new EventTicketSaleResponse(
				ticketId,
				eventId,
				UUID.randomUUID(),
				"VIP",
				UUID.randomUUID(),
				"John Doe",
				1,
				purchaseDate
		);

		when(eventService.getTicketSaleForEvent(eventId, ticketId)).thenReturn(ticketSale);

		mockMvc.perform(get("/api/v1/events/{eventId}/tickets/{ticketId}", eventId, ticketId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(ticketSale.id().toString()))
				.andExpect(jsonPath("$.ticketTypeName").value("VIP"))
				.andExpect(jsonPath("$.buyerName").value("John Doe"))
				.andExpect(jsonPath("$.quantity").value(1));

		verify(eventService).getTicketSaleForEvent(eventId, ticketId);
	}

	@Test
	void shouldGetTicketTypesForEvent() throws Exception {
		UUID eventId = UUID.randomUUID();
		TicketTypeResponse ticketType = new TicketTypeResponse(
				UUID.randomUUID(),
				"Standard",
				"Description1",
				new BigDecimal("50.00"),
				100,
				40,
				60,
				true
		);

		Page<TicketTypeResponse> page = new PageImpl<>(List.of(ticketType), PageRequest.of(0, 10), 1);

		when(eventService.getTicketTypesForEvent(eq(eventId), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/v1/events/{eventId}/ticket-types", eventId)
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(ticketType.id().toString()))
				.andExpect(jsonPath("$.content[0].name").value("Standard"))
				.andExpect(jsonPath("$.content[0].price").value(50.00))
				.andExpect(jsonPath("$.content[0].availableQuantity").value(60))
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.totalElements").value(1));

		verify(eventService).getTicketTypesForEvent(eq(eventId), any(Pageable.class));
	}

	@Test
	void shouldGetTicketTypeForEvent() throws Exception {
		UUID eventId = UUID.randomUUID();
		UUID ticketTypeId = UUID.randomUUID();
		TicketTypeResponse ticketType = new TicketTypeResponse(
				ticketTypeId,
				"VIP",
				"VIP access with backstage pass",
				new BigDecimal("150.00"),
				50,
				25,
				25,
				true
		);

		when(eventService.getTicketTypeForEvent(eventId, ticketTypeId)).thenReturn(ticketType);

		mockMvc.perform(get("/api/v1/events/{eventId}/ticket-types/{ticketTypeId}", eventId, ticketTypeId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(ticketType.id().toString()))
				.andExpect(jsonPath("$.name").value("VIP"))
				.andExpect(jsonPath("$.price").value(150.00))
				.andExpect(jsonPath("$.totalQuantity").value(50))
				.andExpect(jsonPath("$.availableQuantity").value(25));

		verify(eventService).getTicketTypeForEvent(eventId, ticketTypeId);
	}
}
