package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.dtos.UpdateEventRequest;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import com.project.event_ticket_platform.exceptions.EventValidationException;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(
	componentModel = "spring",
	unmappedTargetPolicy = ReportingPolicy.IGNORE,
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class EventMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "organizer", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "ticketTypes", ignore = true)
	@Mapping(target = "status", expression = "java(resolveStatus(request.status()))")
	public abstract Event toEntity(CreateEventRequest request);

	@Mapping(source = "organizer.id", target = "organizerId")
	public abstract EventResponse toResponse(Event event);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "organizer", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "ticketTypes", ignore = true)
	public abstract void updateEvent(UpdateEventRequest request, @MappingTarget Event event);

	protected EventStatus resolveStatus(EventStatus status) {
		return status != null ? status : EventStatus.DRAFT;
	}

	@AfterMapping
	protected void afterCreateMapping(CreateEventRequest request, @MappingTarget Event event) {
		trimStrings(event);
		validateEventTimes(event);
	}

	@AfterMapping
	protected void afterUpdateMapping(UpdateEventRequest request, @MappingTarget Event event) {
		trimStrings(event);
		validateEventTimes(event);
	}

	private void trimStrings(Event event) {
		if (event.getTitle() != null) {
			event.setTitle(event.getTitle().trim());
		}
		if (event.getDescription() != null) {
			event.setDescription(event.getDescription().trim());
		}
		if (event.getLocation() != null) {
			event.setLocation(event.getLocation().trim());
		}
	}

	private void validateEventTimes(Event event) {
		Instant start = event.getStartTime();
		Instant end = event.getEndTime();
		Instant now = Instant.now();

		if (start == null || end == null) {
			throw new EventValidationException("Start and end times are required.");
		}

		if (!end.isAfter(start)) {
			throw new EventValidationException("Event end time must be after the start time.");
		}

		if (start.isBefore(now)) {
			throw new EventValidationException("Event start time must be in the future.");
		}
	}
}
