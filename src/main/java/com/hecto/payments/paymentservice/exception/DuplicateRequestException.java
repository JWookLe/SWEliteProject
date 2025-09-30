package com.hecto.payments.paymentservice.exception;

import com.hecto.payments.paymentservice.domain.Payment;

public class DuplicateRequestException extends RuntimeException {
    private final Payment existingPayment;

    public DuplicateRequestException(Payment existingPayment) {
        super("Idempotency key already used");
        this.existingPayment = existingPayment;
    }

    public Payment getExistingPayment() {
        return existingPayment;
    }
}
