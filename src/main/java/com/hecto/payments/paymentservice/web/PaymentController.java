package com.hecto.payments.paymentservice.web;

import com.hecto.payments.paymentservice.exception.DuplicateRequestException;
import com.hecto.payments.paymentservice.exception.InvalidPaymentStateException;
import com.hecto.payments.paymentservice.exception.PaymentNotFoundException;
import com.hecto.payments.paymentservice.exception.MerchantMismatchException;
import com.hecto.payments.paymentservice.service.PaymentCommandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentCommandService paymentCommandService;

    public PaymentController(PaymentCommandService paymentCommandService) {
        this.paymentCommandService = paymentCommandService;
    }

    @PostMapping("/authorize")
    public ResponseEntity<PaymentResponse> authorize(@Valid @RequestBody AuthorizePaymentRequest request) {
        var payment = paymentCommandService.authorize(
                request.merchantId(),
                request.amount(),
                request.currency(),
                request.idempotencyKey()
        );
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @PostMapping("/capture/{paymentId}")
    public ResponseEntity<PaymentResponse> capture(@PathVariable("paymentId") Long paymentId,
                                                   @Valid @RequestBody CapturePaymentRequest request) {
        var payment = paymentCommandService.capture(paymentId, request.merchantId());
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<PaymentResponse> refund(@PathVariable("paymentId") Long paymentId,
                                                  @Valid @RequestBody RefundPaymentRequest request) {
        var payment = paymentCommandService.refund(paymentId, request.merchantId(), request.reason());
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ProblemDetailResponse> handleDuplicate(DuplicateRequestException ex) {
        var body = new ProblemDetailResponse("DUPLICATE_REQUEST", ex.getMessage(),
                ex.getExistingPayment().getId());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ProblemDetailResponse> handleNotFound(PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ProblemDetailResponse("PAYMENT_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ProblemDetailResponse> handleInvalidState(InvalidPaymentStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ProblemDetailResponse("INVALID_STATE", ex.getMessage(), null));
    }

    @ExceptionHandler(MerchantMismatchException.class)
    public ResponseEntity<ProblemDetailResponse> handleMerchantMismatch(MerchantMismatchException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ProblemDetailResponse("MERCHANT_MISMATCH", ex.getMessage(), null));
    }
}
