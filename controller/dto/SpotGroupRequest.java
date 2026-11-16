package com.parking.controller.dto;

import com.parking.model.SpotType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SpotGroupRequest(
        @NotNull SpotType type,
        @Min(0) int floor,
        @Min(1) @Max(100_000) int count) {
}
