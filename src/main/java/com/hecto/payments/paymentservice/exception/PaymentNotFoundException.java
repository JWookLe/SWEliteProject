package com.hecto.payments.paymentservice.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long paymentId) {
        super("Payment %d not found".formatted(paymentId));
    }
}
