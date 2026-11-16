package com.parking.controller.dto;

import com.parking.model.VehicleType;
import java.time.Instant;

public record TicketResponse(
        String ticketId, String lotId, String licensePlate, VehicleType vehicleType, SpotResponse spot, Instant entryTime) {
}
