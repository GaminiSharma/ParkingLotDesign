package com.parking.controller.dto;

import com.parking.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ParkVehicleRequest(
        @NotBlank @Size(max = 20) String licensePlate,
        @NotNull VehicleType vehicleType) {
}
