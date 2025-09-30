package com.hecto.payments.paymentservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void authorizeCreatesPayment() throws Exception {
        var request = new AuthorizeRequest("M100", 10_000L, "KRW", "idem-1");

        mockMvc.perform(post("/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value("M100"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void duplicateAuthorizationReturnsConflict() throws Exception {
        var request = new AuthorizeRequest("M101", 5_000L, "KRW", "dup-key");
        mockMvc.perform(post("/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_REQUEST"))
                .andExpect(jsonPath("$.paymentId").isNumber());
    }

    @Test
    void captureAndRefundTransitionsPayment() throws Exception {
        var authorizeRequest = new AuthorizeRequest("M102", 7_500L, "KRW", "idem-2");
        MvcResult result = mockMvc.perform(post("/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        var response = objectMapper.readTree(result.getResponse().getContentAsString());
        long paymentId = response.get("paymentId").asLong();

        var captureRequest = new CaptureRequest("M102");
        mockMvc.perform(post("/payments/capture/" + paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(captureRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        var refundRequest = new RefundRequest("M102", "customer cancellation");
        mockMvc.perform(post("/payments/refund/" + paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    private record AuthorizeRequest(String merchantId, Long amount, String currency, String idempotencyKey) {
    }

    private record CaptureRequest(String merchantId) {
    }

    private record RefundRequest(String merchantId, String reason) {
    }
}
