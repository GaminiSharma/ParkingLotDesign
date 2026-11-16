package com.parking.payment;


public sealed interface PaymentResult {
    record Success(String reference) implements PaymentResult {}

    record Failure(String reason) implements PaymentResult {}
}
