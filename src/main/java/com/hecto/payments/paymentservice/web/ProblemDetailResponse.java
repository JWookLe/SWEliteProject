package com.hecto.payments.paymentservice.web;

public record ProblemDetailResponse(
        String code,
        String message,
        Long paymentId
) {
}
