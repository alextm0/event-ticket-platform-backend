package com.project.event_ticket_platform.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Request to assign a staff member to an event")
public record AssignStaffRequest(
        @Schema(description = "User ID of the staff member", required = true) UUID staffId) {
}
