package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.dtos.UpdateEventRequest;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.EventStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

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
	}

	@AfterMapping
	protected void afterUpdateMapping(UpdateEventRequest request, @MappingTarget Event event) {
		trimStrings(event);
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

}
