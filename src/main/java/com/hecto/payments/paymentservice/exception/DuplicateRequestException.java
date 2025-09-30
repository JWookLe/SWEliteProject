package com.hecto.payments.paymentservice.exception;

import com.hecto.payments.paymentservice.domain.Payment;

public class DuplicateRequestException extends RuntimeException {
    private final Payment existingPayment;

    public DuplicateRequestException(Payment existingPayment) {
        this(existingPayment, "Idempotency key already used");
    }

    public DuplicateRequestException(Payment existingPayment, String message) {
        super(message);
        this.existingPayment = existingPayment;
    }

    public Payment getExistingPayment() {
        return existingPayment;
    }
}
