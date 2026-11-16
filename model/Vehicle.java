package com.parking.model;

import java.util.Locale;
import java.util.Objects;

public record Vehicle(String licensePlate, VehicleType type) {
    public Vehicle {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(licensePlate, "licensePlate");
        licensePlate = licensePlate.trim().toUpperCase(Locale.ROOT); // "ka01 ab" and "KA01 AB" are one vehicle
        if (licensePlate.isEmpty()) {
            throw new IllegalArgumentException("licensePlate must not be blank");
        }
    }
}
