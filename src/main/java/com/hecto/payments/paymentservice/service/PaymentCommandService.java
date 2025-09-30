package com.hecto.payments.paymentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hecto.payments.paymentservice.domain.LedgerEntry;
import com.hecto.payments.paymentservice.domain.OutboxEvent;
import com.hecto.payments.paymentservice.domain.Payment;
import com.hecto.payments.paymentservice.domain.PaymentStatus;
import com.hecto.payments.paymentservice.exception.DuplicateRequestException;
import com.hecto.payments.paymentservice.exception.InvalidPaymentStateException;
import com.hecto.payments.paymentservice.exception.PaymentNotFoundException;
import com.hecto.payments.paymentservice.exception.MerchantMismatchException;
import com.hecto.payments.paymentservice.repository.LedgerEntryRepository;
import com.hecto.payments.paymentservice.repository.OutboxEventRepository;
import com.hecto.payments.paymentservice.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public PaymentCommandService(PaymentRepository paymentRepository,
                                 LedgerEntryRepository ledgerEntryRepository,
                                 OutboxEventRepository outboxEventRepository,
                                 ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Payment authorize(String merchantId, long amount, String currency, String idempotencyKey) {
        paymentRepository.findByMerchantIdAndIdempotencyKey(merchantId, idempotencyKey)
                .ifPresent(existing -> {
                    throw new DuplicateRequestException(existing);
                });

        Payment payment = new Payment(merchantId, amount, currency.toUpperCase(), PaymentStatus.REQUESTED, idempotencyKey);
        payment.markRequested();
        Payment saved = paymentRepository.save(payment);

        recordEvent(saved, "PAYMENT_AUTHORIZED", Map.of(
                "paymentId", saved.getId(),
                "merchantId", saved.getMerchantId(),
                "amount", saved.getAmount(),
                "currency", saved.getCurrency(),
                "status", saved.getStatus().name()
        ));
        return saved;
    }

    @Transactional
    public Payment capture(Long paymentId, String merchantId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (!payment.getMerchantId().equals(merchantId)) {
            throw new MerchantMismatchException(paymentId);
        }

        if (payment.getStatus() != PaymentStatus.REQUESTED) {
            throw new InvalidPaymentStateException(paymentId, PaymentStatus.REQUESTED, payment.getStatus());
        }

        payment.markCompleted();
        Payment saved = paymentRepository.save(payment);

        ledgerEntryRepository.save(new LedgerEntry(payment, "merchant_receivable", "cash", payment.getAmount()));

        recordEvent(saved, "PAYMENT_CAPTURED", Map.of(
                "paymentId", saved.getId(),
                "merchantId", saved.getMerchantId(),
                "amount", saved.getAmount(),
                "status", saved.getStatus().name()
        ));
        return saved;
    }

    @Transactional
    public Payment refund(Long paymentId, String merchantId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (!payment.getMerchantId().equals(merchantId)) {
            throw new MerchantMismatchException(paymentId);
        }

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException(paymentId, PaymentStatus.COMPLETED, payment.getStatus());
        }

        payment.markRefunded();
        Payment saved = paymentRepository.save(payment);

        ledgerEntryRepository.save(new LedgerEntry(payment, "cash", "merchant_receivable", payment.getAmount()));

        recordEvent(saved, "PAYMENT_REFUNDED", Map.of(
                "paymentId", saved.getId(),
                "merchantId", saved.getMerchantId(),
                "amount", saved.getAmount(),
                "status", saved.getStatus().name(),
                "reason", reason
        ));
        return saved;
    }

    private void recordEvent(Payment payment, String eventType, Map<String, Object> payload) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            outboxEventRepository.save(new OutboxEvent(
                    "PAYMENT",
                    payment.getId(),
                    eventType,
                    body
            ));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize event payload", ex);
        }
    }
}
