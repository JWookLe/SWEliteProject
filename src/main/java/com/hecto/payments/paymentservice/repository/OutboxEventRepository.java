package com.hecto.payments.paymentservice.repository;

import com.hecto.payments.paymentservice.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
}
