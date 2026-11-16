package com.parking.controller.dto;

import com.parking.model.SpotType;

public record SpotResponse(int number, int floor, SpotType type) {
}
