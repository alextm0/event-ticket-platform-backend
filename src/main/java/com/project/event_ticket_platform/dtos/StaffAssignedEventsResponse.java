package com.project.event_ticket_platform.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response containing all events assigned to a staff member")
public record StaffAssignedEventsResponse(
	@Schema(description = "List of assigned events with their IDs and names")
	List<AssignedEventInfo> events
) {
}

