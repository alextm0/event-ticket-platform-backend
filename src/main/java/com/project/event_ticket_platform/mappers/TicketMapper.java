package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.EventTicketSaleResponse;
import com.project.event_ticket_platform.dtos.TicketResponse;
import com.project.event_ticket_platform.entities.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
	componentModel = "spring",
	unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TicketMapper {

	/**
	 * Map a Ticket entity to a TicketResponse DTO.
	 *
	 * <p>The resulting DTO includes the ticket's QR code id, event title, event location,
	 * event start time, and ticket type name.</p>
	 *
	 * @param ticket the Ticket entity to convert
	 * @return a TicketResponse populated from the given Ticket (includes qrCodeId, eventTitle,
	 *         eventLocation, eventStartTime, and ticketTypeName)
	 */
	@Mapping(source = "qrCode.id", target = "qrCodeId")
	@Mapping(source = "ticketType.event.title", target = "eventTitle")
	@Mapping(source = "ticketType.event.location", target = "eventLocation")
	@Mapping(source = "ticketType.event.startTime", target = "eventStartTime")
	@Mapping(source = "ticketType.name", target = "ticketTypeName")
	TicketResponse toResponse(Ticket ticket);

	/**
	 * Creates an EventTicketSaleResponse populated with sale and ticket details from the given Ticket.
	 *
	 * The response includes the ticket id, associated event id, ticket type id and name, buyer id and name,
	 * purchase date, and quantity.
	 *
	 * @param ticket the Ticket entity to convert
	 * @return an EventTicketSaleResponse containing sale and ticket information extracted from the ticket
	 */
	@Mapping(source = "id", target = "id")
	@Mapping(source = "ticketType.event.id", target = "eventId")
	@Mapping(source = "ticketType.id", target = "ticketTypeId")
	@Mapping(source = "ticketType.name", target = "ticketTypeName")
	@Mapping(source = "order.user.id", target = "buyerId")
	@Mapping(source = "order.buyerName", target = "buyerName")
	@Mapping(source = "order.createdAt", target = "purchaseDate")
	@Mapping(source = "order.totalAmount", target = "quantity")
	EventTicketSaleResponse toEventTicketSaleResponse(Ticket ticket);
}
