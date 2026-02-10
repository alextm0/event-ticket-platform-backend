package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Used by Spring Security SpEL in @PreAuthorize to enforce "organizer of event" checks.
 */
@Service("eventAuthorizationService")
@RequiredArgsConstructor
public class EventAuthorizationService {

    private final EventRepository eventRepository;

    /**
     * Returns true if the given user is the organizer of the event.
     * Used from @PreAuthorize("@eventAuthorizationService.isOrganizer(#eventId, authentication.principal)").
     */
    public boolean isOrganizer(UUID eventId, UUID userId) {
        if (eventId == null || userId == null) {
            return false;
        }
        return eventRepository.findById(eventId)
                .map(event -> userId.equals(event.getOrganizer().getId()))
                .orElse(false);
    }
}
