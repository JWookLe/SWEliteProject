package com.hecto.payments.paymentservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "payment", indexes = {
        @Index(name = "ix_status_time", columnList = "status, requested_at"),
        @Index(name = "ix_merchant_time", columnList = "merchant_id, requested_at")
}, uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(name = "uk_merchant_idem", columnNames = {"merchant_id", "idempotency_key"})
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(name = "merchant_id", nullable = false, length = 32)
    private String merchantId;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Payment() {
    }

    public Payment(String merchantId, Long amount, String currency, PaymentStatus status, String idempotencyKey) {
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.requestedAt = Instant.now();
        this.updatedAt = this.requestedAt;
    }

    public void markRequested() {
        this.status = PaymentStatus.REQUESTED;
        touch();
    }

    public void markCompleted() {
        this.status = PaymentStatus.COMPLETED;
        touch();
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
        touch();
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
