package com.project.event_ticket_platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Entity
@Table(name = "ticket_validations")
@EntityListeners(AuditingEntityListener.class)
public class TicketValidation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private TicketValidationEnum status;

	@CreatedDate
	@Column(name = "validation_date_time", nullable = false, updatable = false)
	private Instant validationDateTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "validation_method", nullable = false)
	private TicketValidationMethodEnum validationMethod;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private Ticket ticket;
}
