package com.project.event_ticket_platform.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@GetMapping
	public Map<String, Object> getStatus() {
		return Map.of("status", "UP", "timestamp", Instant.now().toString());
	}
}
