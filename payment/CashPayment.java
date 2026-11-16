package com.parking.payment;

import java.math.BigDecimal;
import java.util.UUID;

public final class CashPayment implements PaymentStrategy {
    @Override
    public PaymentResult pay(BigDecimal amount) {
        return new PaymentResult.Success("cash-" + UUID.randomUUID());
    }
}
