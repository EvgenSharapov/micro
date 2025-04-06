package org.example.paymentservice.service;

import org.example.commonevents.dto.PaymentRequest;
import org.example.commonevents.dto.PaymentResponse;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        return PaymentResponse.builder()
                .paymentId("PAY-" + System.currentTimeMillis())
                .orderId(paymentRequest.getOrderId())
                .status("COMPLETED")
                .amount(paymentRequest.getAmount())
                .build();
    }
}