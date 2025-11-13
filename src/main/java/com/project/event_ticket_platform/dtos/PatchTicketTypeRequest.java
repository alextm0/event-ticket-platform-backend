package com.project.event_ticket_platform.dtos;

import java.math.BigDecimal;
import java.util.Optional;

public record PatchTicketTypeRequest(
        Optional<String> name,
        Optional<String> description,
        Optional<BigDecimal> price,
        Optional<Integer> quantity,
        Optional<Boolean> active
) {
}