package com.hecto.payments.paymentservice.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefundPaymentRequest(
        @NotBlank @Size(max = 32) String merchantId,
        @NotBlank @Size(max = 128) String reason
) {
}
