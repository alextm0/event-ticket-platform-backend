package com.project.event_ticket_platform.dtos;

import java.math.BigDecimal;

public record SalesHistoryPoint(
        String date,
        BigDecimal revenue,
        Long sales) {
}
