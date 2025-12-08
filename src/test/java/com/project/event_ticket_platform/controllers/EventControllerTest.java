package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

	@Test
	void shouldCreateTicketTypeForEvent() throws Exception {
		UUID eventId = UUID.randomUUID();
		UUID ticketTypeId = UUID.randomUUID();

		TicketTypeResponse response = new TicketTypeResponse(
				ticketTypeId,
				"VIP",
				"VIP access",
				new BigDecimal("150.00"),
				50,
				0,
				50,
				true
		);

		when(eventService.createTicketTypeForEvent(eq(eventId), any(CreateTicketTypeRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/v1/events/{eventId}/ticket-types", eventId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "VIP",
								  "description": "VIP access",
								  "price": 150.00,
								  "totalQuantity": 50,
								  "active": true
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/events/" + eventId + "/ticket-types/" + ticketTypeId))
				.andExpect(jsonPath("$.id").value(ticketTypeId.toString()))
				.andExpect(jsonPath("$.name").value("VIP"))
				.andExpect(jsonPath("$.price").value(150.00))
				.andExpect(jsonPath("$.totalQuantity").value(50))
				.andExpect(jsonPath("$.availableQuantity").value(50));

		verify(eventService).createTicketTypeForEvent(eq(eventId), any(CreateTicketTypeRequest.class));
	}

	@Test
	void shouldDeleteTicketTypeForEvent() throws Exception {
		UUID eventId = UUID.randomUUID();
		UUID ticketTypeId = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/events/{eventId}/ticket-types/{ticketTypeId}", eventId, ticketTypeId))
				.andExpect(status().isNoContent());

		verify(eventService).deleteTicketTypeForEvent(eventId, ticketTypeId);
	}

	@Test
	void shouldPatchTicketTypeForEvent() throws Exception {
		UUID eventId = UUID.randomUUID();
		UUID ticketTypeId = UUID.randomUUID();
		TicketTypeResponse response = new TicketTypeResponse(
				ticketTypeId,
				"VIP Silver",
				"VIP access with backstage pass and complimentary drinks",
				new BigDecimal("200.00"),
				75,
				50,
				25,
				true
		);

		when(eventService.patchTicketTypeForEvent(eq(eventId), eq(ticketTypeId), any(PatchTicketTypeRequest.class)))
				.thenReturn(response);

		mockMvc.perform(patch("/api/v1/events/{eventId}/ticket-types/{ticketTypeId}", eventId, ticketTypeId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
                                 {
                                   "name": "VIP Silver",
                                   "price": 200.00,
                                   "quantity": 25
                                 }
                                 """))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(ticketTypeId.toString()))
				.andExpect(jsonPath("$.name").value("VIP Silver"))
				.andExpect(jsonPath("$.price").value(200.00))
				.andExpect(jsonPath("$.availableQuantity").value(25));

		verify(eventService).patchTicketTypeForEvent(eq(eventId), eq(ticketTypeId), any(PatchTicketTypeRequest.class));
	}

	@Test
	void shouldGetAssignedEventsForStaff() throws Exception {
		UUID staffId = UUID.randomUUID();
		UUID eventId1 = UUID.randomUUID();
		UUID eventId2 = UUID.randomUUID();
		AssignedEventInfo event1 = new AssignedEventInfo(eventId1, "Spring Music Festival");
		AssignedEventInfo event2 = new AssignedEventInfo(eventId2, "Summer Tech Conference");
		StaffAssignedEventsResponse response = new StaffAssignedEventsResponse(List.of(event1, event2));

		when(eventService.getAssignedEventsForStaff(staffId)).thenReturn(response);

		mockMvc.perform(get("/api/v1/events/staff/{staffId}/assigned-events", staffId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.events").isArray())
			.andExpect(jsonPath("$.events[0].eventId").value(eventId1.toString()))
			.andExpect(jsonPath("$.events[0].eventName").value("Spring Music Festival"))
			.andExpect(jsonPath("$.events[1].eventId").value(eventId2.toString()))
			.andExpect(jsonPath("$.events[1].eventName").value("Summer Tech Conference"))
			.andExpect(jsonPath("$.events.length()").value(2));

		verify(eventService).getAssignedEventsForStaff(staffId);
	}

	@Test
	void shouldReturnEmptyListWhenStaffHasNoAssignedEvents() throws Exception {
		UUID staffId = UUID.randomUUID();
		StaffAssignedEventsResponse response = new StaffAssignedEventsResponse(List.of());

		when(eventService.getAssignedEventsForStaff(staffId)).thenReturn(response);

		mockMvc.perform(get("/api/v1/events/staff/{staffId}/assigned-events", staffId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.events").isArray())
			.andExpect(jsonPath("$.events.length()").value(0));

		verify(eventService).getAssignedEventsForStaff(staffId);
	}
}
