package com.parking.pricing;

import com.parking.model.VehicleType;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

public final class HourlyRatePricing implements PricingStrategy {
    private final Map<VehicleType, BigDecimal> hourlyRates;

    public HourlyRatePricing(Map<VehicleType, BigDecimal> hourlyRates) {
        this.hourlyRates = new EnumMap<>(VehicleType.class);
        this.hourlyRates.putAll(hourlyRates);
        for (VehicleType t : VehicleType.values()) {
            if (!this.hourlyRates.containsKey(t)) {
                throw new IllegalArgumentException("Missing hourly rate for " + t);
            }
        }
    }

    public static HourlyRatePricing basic() {
        return of(5, 8, 10, 15);
    }

    public static HourlyRatePricing premium() {
        return of(8, 12, 15, 20);
    }

    private static HourlyRatePricing of(int bike, int auto, int car, int truck) {
        return new HourlyRatePricing(Map.of(
                VehicleType.BIKE, BigDecimal.valueOf(bike),
                VehicleType.AUTO, BigDecimal.valueOf(auto),
                VehicleType.CAR, BigDecimal.valueOf(car),
                VehicleType.TRUCK, BigDecimal.valueOf(truck)));
    }

    @Override
    public BigDecimal feeFor(VehicleType vehicleType, Duration parked) {
        long seconds = Math.max(0, parked.getSeconds()) + (parked.getNano() > 0 ? 1 : 0);
        long hours = Math.max(1, (seconds + 3599) / 3600);
        return hourlyRates.get(vehicleType).multiply(BigDecimal.valueOf(hours)).setScale(2);
    }
}
