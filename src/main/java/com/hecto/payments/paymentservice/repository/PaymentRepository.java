package com.hecto.payments.paymentservice.repository;

import com.hecto.payments.paymentservice.domain.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMerchantIdAndIdempotencyKey(String merchantId, String idempotencyKey);
}
