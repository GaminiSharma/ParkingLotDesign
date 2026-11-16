package com.parking.pricing;

import com.parking.model.VehicleType;
import java.math.BigDecimal;
import java.time.Duration;

public interface PricingStrategy {
    BigDecimal feeFor(VehicleType vehicleType, Duration parked);
}
