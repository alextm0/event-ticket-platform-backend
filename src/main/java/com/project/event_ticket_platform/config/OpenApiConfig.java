package com.project.event_ticket_platform.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
	info = @Info(
		title = "Event Ticket Platform API",
		version = "v1",
		description = "REST API for managing users, events, tickets, and validations.",
		license = @License(name = "Apache 2.0")
	),
	servers = {
		@Server(url = "/")
	}
)
public class OpenApiConfig {
	// Marker configuration for OpenAPI metadata.
}
