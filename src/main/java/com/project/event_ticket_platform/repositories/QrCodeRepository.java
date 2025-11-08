package com.project.event_ticket_platform.repositories;

import com.project.event_ticket_platform.entities.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
}

