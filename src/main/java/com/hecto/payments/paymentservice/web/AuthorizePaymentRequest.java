package com.hecto.payments.paymentservice.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuthorizePaymentRequest(
        @NotBlank @Size(max = 32) String merchantId,
        @NotNull @Min(1) Long amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank @Size(max = 64) String idempotencyKey
) {
}
