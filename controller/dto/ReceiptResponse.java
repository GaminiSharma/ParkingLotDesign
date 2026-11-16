package com.parking.controller.dto;

import com.parking.model.VehicleType;
import java.math.BigDecimal;
import java.time.Instant;

public record ReceiptResponse(
        String ticketId, String licensePlate, VehicleType vehicleType, SpotResponse spot,
        Instant entryTime, Instant exitTime, BigDecimal fee, String paymentReference) {
}
