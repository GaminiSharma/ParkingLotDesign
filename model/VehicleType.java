package com.parking.model;

import java.util.List;

public enum VehicleType {
    BIKE(SpotType.BIKE, SpotType.CAR),   
    AUTO(SpotType.CAR),
    CAR(SpotType.CAR),
    TRUCK(SpotType.LARGE);

    private final List<SpotType> acceptableSpots;

    VehicleType(SpotType... acceptableSpots) {
        this.acceptableSpots = List.of(acceptableSpots);
    }

    public List<SpotType> acceptableSpots() {
        return acceptableSpots;
    }
}
