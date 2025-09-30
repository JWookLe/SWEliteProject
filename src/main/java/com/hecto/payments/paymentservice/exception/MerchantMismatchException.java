package com.hecto.payments.paymentservice.exception;

public class MerchantMismatchException extends RuntimeException {
    public MerchantMismatchException(Long paymentId) {
        super("Payment %d does not belong to the provided merchant".formatted(paymentId));
    }
}
