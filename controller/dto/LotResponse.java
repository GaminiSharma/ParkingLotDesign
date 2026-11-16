package com.parking.controller.dto;

import com.parking.application.PricingPlan;
import com.parking.model.SpotType;
import java.util.Map;

public record LotResponse(String lotId, PricingPlan pricingPlan, Map<SpotType, Integer> capacity, Map<SpotType, Integer> available) {
}
