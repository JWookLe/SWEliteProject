package com.hecto.payments.paymentservice.web;

import com.hecto.payments.paymentservice.domain.Payment;
import com.hecto.payments.paymentservice.domain.PaymentStatus;
import java.time.Instant;

public record PaymentResponse(
        Long paymentId,
        String merchantId,
        Long amount,
        String currency,
        PaymentStatus status,
        Instant requestedAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMerchantId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getRequestedAt(),
                payment.getUpdatedAt()
        );
    }
}
