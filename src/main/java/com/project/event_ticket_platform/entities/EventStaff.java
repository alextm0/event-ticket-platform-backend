package com.project.event_ticket_platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Table(
	name = "event_staff",
	uniqueConstraints = @UniqueConstraint(name = "uq_event_staff_assignment", columnNames = { "event_id", "staff_id" })
)
@EntityListeners(AuditingEntityListener.class)
public class EventStaff {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "staff_id", nullable = false)
	private User staff;

	@CreatedDate
	@Column(name = "assigned_at", nullable = false, updatable = false)
	private Instant assignedAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
