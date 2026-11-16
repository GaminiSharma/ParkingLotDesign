package com.parking.controller.dto;

import com.parking.application.PricingPlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record CreateLotRequest(
        @NotNull @Pattern(regexp = "^[A-Za-z0-9_-]{1,32}$", message = "must be 1-32 letters, digits, '_' or '-'") String lotId,
        @NotNull PricingPlan pricingPlan,
        @NotEmpty List<@Valid @NotNull SpotGroupRequest> spots) {
}
