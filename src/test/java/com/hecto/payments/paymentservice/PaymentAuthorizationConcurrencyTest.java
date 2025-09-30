package com.hecto.payments.paymentservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.hecto.payments.paymentservice.domain.Payment;
import com.hecto.payments.paymentservice.repository.PaymentRepository;
import com.hecto.payments.paymentservice.service.PaymentCommandService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaymentAuthorizationConcurrencyTest {

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void concurrentDuplicateAuthorizationsReuseOriginalPayment() throws Exception {
        String merchantId = "CONCURRENT-M100";
        String idempotencyKey = "concurrent-idem";

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        List<Payment> results = new ArrayList<>();

        boolean allWorkersReady = false;
        try {
            for (int i = 0; i < 2; i++) {
                executorService.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        synchronized (results) {
                            results.add(paymentCommandService.authorize(merchantId, 12_345L, "KRW", idempotencyKey));
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            allWorkersReady = ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } finally {
            executorService.shutdownNow();
        }

        assertThat(allWorkersReady).isTrue();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isNotNull();
        assertThat(results.get(0).getId()).isEqualTo(results.get(1).getId());
        assertThat(results.get(0).getStatus()).isEqualTo(results.get(1).getStatus());
    }
}
