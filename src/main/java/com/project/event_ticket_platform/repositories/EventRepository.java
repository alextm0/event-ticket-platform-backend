package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

	@Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.ticketTypes LEFT JOIN FETCH e.organizer WHERE e.id IN :ids")
	List<Event> findAllByIdsWithRelations(@Param("ids") List<UUID> ids);

	@Query(value = "SELECT e.id FROM events e WHERE e.status::text = :status ORDER BY e.created_at DESC", nativeQuery = true)
	List<UUID> findEventIdsByStatusNative(@Param("status") String status);

	default Page<Event> findAllByStatus(EventStatus status, Pageable pageable) {
		// Get all matching IDs
		List<UUID> allIds = findEventIdsByStatusNative(status.name());
		
		if (allIds.isEmpty()) {
			return Page.empty(pageable);
		}
		
		// Apply pagination manually
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), allIds.size());
		
		if (start >= allIds.size()) {
			return Page.empty(pageable);
		}
		
		List<UUID> paginatedIds = allIds.subList(start, end);
		
		// Fetch events with relationships
		List<Event> events = findAllByIdsWithRelations(paginatedIds);
		
		// Preserve order from IDs
		Map<UUID, Event> eventMap = new LinkedHashMap<>();
		for (Event event : events) {
			eventMap.put(event.getId(), event);
		}
		List<Event> orderedEvents = new ArrayList<>();
		for (UUID id : paginatedIds) {
			Event event = eventMap.get(id);
			if (event != null) {
				orderedEvents.add(event);
			}
		}
		
		return new PageImpl<>(orderedEvents, pageable, allIds.size());
	}
}
