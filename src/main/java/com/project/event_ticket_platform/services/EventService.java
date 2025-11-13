package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {

	EventResponse createEvent(CreateEventRequest request);

	Page<EventResponse> getAllEvents(Pageable pageable);

	/**
 * Updates the event identified by eventId with the data from the provided request.
 *
 * @param eventId the identifier of the event to update
 * @param request the data to apply to the event
 * @return the updated EventResponse representing the event after the update
 */
EventResponse updateEvent(UUID eventId, UpdateEventRequest request);

	/**
 * Deletes the event identified by the given ID.
 *
 * @param eventId the UUID of the event to delete
 */
void deleteEvent(UUID eventId);

	/**
 * Retrieves ticket sale records for the specified event in a paginated form.
 *
 * @param eventId the UUID of the event whose ticket sales should be retrieved
 * @param pageable pagination and sorting information for the result set
 * @return a page of EventTicketSaleResponse containing ticket sale records for the given event
 */
Page<EventTicketSaleResponse> getTicketSalesForEvent(UUID eventId, Pageable pageable);

    /**
 * Retrieves a specific ticket sale associated with the given event.
 *
 * @param eventId the event's UUID
 * @param ticketId the ticket sale's UUID
 * @return the ticket sale as an EventTicketSaleResponse
 */
EventTicketSaleResponse getTicketSaleForEvent(UUID eventId, UUID ticketId);

	/**
 * Retrieve paginated ticket types for a specific event.
 *
 * @param eventId the UUID of the event whose ticket types are being retrieved
 * @param pageable pagination and sorting information for the result set
 * @return a Page of TicketTypeResponse containing the ticket types for the specified event
 */
Page<TicketTypeResponse> getTicketTypesForEvent(UUID eventId, Pageable pageable);

	/**
 * Retrieves a specific ticket type for a given event.
 *
 * @param eventId      the UUID of the event
 * @param ticketTypeId the UUID of the ticket type
 * @return              the ticket type details for the specified event and ticket type
 */
TicketTypeResponse getTicketTypeForEvent(UUID eventId, UUID ticketTypeId);

	/**
 * Deletes the specified ticket type belonging to the given event.
 *
 * @param eventId the UUID of the event that owns the ticket type
 * @param ticketTypeId the UUID of the ticket type to delete
 */
void deleteTicketTypeForEvent(UUID eventId, UUID ticketTypeId);

	/**
 * Partially updates a ticket type belonging to the specified event.
 *
 * @param eventId the UUID of the event that owns the ticket type
 * @param ticketTypeId the UUID of the ticket type to patch
 * @param request the patch data describing fields to update on the ticket type
 * @return the updated TicketTypeResponse representing the ticket type after the patch
 */
TicketTypeResponse patchTicketTypeForEvent(UUID eventId, UUID ticketTypeId, PatchTicketTypeRequest request);
}