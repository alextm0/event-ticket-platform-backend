package com.project.event_ticket_platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "users")
@org.hibernate.annotations.SQLInsert(
	sql = "INSERT INTO users (created_at, email, name, password_hash, role, updated_at, id) " +
		  "VALUES (?, ?, ?, ?, CAST(? AS user_role), ?, ?)"
)
@org.hibernate.annotations.SQLUpdate(
	sql = "UPDATE users SET email = ?, name = ?, password_hash = ?, " +
		  "role = CAST(? AS user_role), updated_at = ? WHERE id = ?"
)
@EntityListeners(AuditingEntityListener.class)
public class User {

	@Id
	private UUID id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private UserRole role;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY)
	private List<Event> organizedEvents = new ArrayList<>();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<TicketOrder> orders = new ArrayList<>();

	@OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
	private List<EventStaff> staffAssignments = new ArrayList<>();
}
