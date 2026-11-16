package com.parking.lot;

import com.parking.model.Ticket;
import java.math.BigDecimal;
import java.time.Instant;

public sealed interface ExitResult {
    record Completed(Ticket ticket, BigDecimal fee, Instant exitTime, String paymentReference) implements ExitResult {}

    record PaymentFailed(String reason) implements ExitResult {}

    record UnknownTicket(String ticketId) implements ExitResult {}
}
