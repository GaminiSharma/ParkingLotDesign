package com.parking.payment;

import java.math.BigDecimal;
import java.util.UUID;

public final class CreditCardPayment implements PaymentStrategy {
    @Override
    public PaymentResult pay(BigDecimal amount) {
        return new PaymentResult.Success("card-" + UUID.randomUUID());
    }
}
