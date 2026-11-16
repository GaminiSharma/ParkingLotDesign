package com.parking.model;

import java.util.Comparator;
import java.util.Objects;

public record ParkingSpot(int number, int floor, SpotType type) implements Comparable<ParkingSpot> {
    private static final Comparator<ParkingSpot> ORDER = Comparator
            .comparingInt(ParkingSpot::floor)
            .thenComparingInt(ParkingSpot::number)
            .thenComparing(ParkingSpot::type);

    public ParkingSpot {
        Objects.requireNonNull(type, "type");
        if (number < 0 || floor < 0) {
            throw new IllegalArgumentException("number and floor must be >= 0");
        }
    }

    @Override
    public int compareTo(ParkingSpot other) {
        return ORDER.compare(this, other);
    }
}
