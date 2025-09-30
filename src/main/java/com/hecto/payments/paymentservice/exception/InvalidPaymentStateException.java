package com.hecto.payments.paymentservice.exception;

import com.hecto.payments.paymentservice.domain.PaymentStatus;

public class InvalidPaymentStateException extends RuntimeException {
    public InvalidPaymentStateException(Long paymentId, PaymentStatus expected, PaymentStatus actual) {
        super("Payment %d expected in state %s but was %s".formatted(paymentId, expected, actual));
    }
}
