package com.parking.lot;

import com.parking.model.Ticket;
import com.parking.model.VehicleType;

public sealed interface ParkResult {
    record Parked(Ticket ticket) implements ParkResult {}

    /** Idempotent retry */
    record AlreadyParked(Ticket ticket) implements ParkResult {}

    record LotFull(VehicleType vehicleType) implements ParkResult {}
}
