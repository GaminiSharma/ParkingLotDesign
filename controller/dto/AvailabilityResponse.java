package com.parking.controller.dto;

import com.parking.model.SpotType;
import java.time.Instant;
import java.util.Map;

/** Counters are O(1) and eventually consistent, hence {@code asOf}. Entry is the authority. */
public record AvailabilityResponse(String lotId, Map<SpotType, Integer> available, Instant asOf) {
}
