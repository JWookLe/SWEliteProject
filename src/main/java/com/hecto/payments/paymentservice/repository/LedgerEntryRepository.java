package com.hecto.payments.paymentservice.repository;

import com.hecto.payments.paymentservice.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
}
