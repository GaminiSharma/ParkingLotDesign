package com.parking.controller.dto;

import com.parking.application.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record ExitTicketRequest(@NotNull PaymentMethod paymentMethod) {
}
