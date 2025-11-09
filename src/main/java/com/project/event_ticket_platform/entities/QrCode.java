package com.project.event_ticket_platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "qr_codes")
@org.hibernate.annotations.SQLInsert(
	sql = "INSERT INTO qr_codes (code_data, generated_date_time, status, updated_at, id) " +
		  "VALUES (?, ?, CAST(? AS qr_code_status), ?, ?)"
)
@org.hibernate.annotations.SQLUpdate(
	sql = "UPDATE qr_codes SET code_data = ?, status = CAST(? AS qr_code_status), updated_at = ? WHERE id = ?"
)
@EntityListeners(AuditingEntityListener.class)
public class QrCode {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "code_data", columnDefinition = "TEXT")
	private String codeData;

	@CreatedDate
	@Column(name = "generated_date_time", nullable = false, updatable = false)
	private Instant generatedDateTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, columnDefinition = "qr_code_status")
	private QrCodeStatusEnum status = QrCodeStatusEnum.ACTIVE;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
