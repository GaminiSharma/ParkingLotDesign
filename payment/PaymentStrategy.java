package com.parking.payment;

import java.math.BigDecimal;

public interface PaymentStrategy {
    PaymentResult pay(BigDecimal amount);
}
