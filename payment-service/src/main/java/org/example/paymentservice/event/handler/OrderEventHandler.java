package org.example.paymentservice.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commonevents.dto.PaymentRequest;
import org.example.commonevents.event.OrderEvent;
import org.example.commonevents.event.PaymentEvent;
import org.example.paymentservice.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {
    private final PaymentService paymentService;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void handleOrderEvent(OrderEvent orderEvent) {
        log.info("Received order event: {}", orderEvent);

        try {
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .orderId(orderEvent.getOrderId())
                    .userId(orderEvent.getUserId())
                    .amount((double) (orderEvent.getQuantity() * 100))
                    .build();

            paymentService.processPayment(paymentRequest);

            PaymentEvent paymentEvent = PaymentEvent.builder()
                    .orderId(orderEvent.getOrderId())
                    .status("PAYMENT_COMPLETED")
                    .build();

            kafkaTemplate.send("payment-events", paymentEvent);
        } catch (Exception e) {
            log.error("Payment processing failed: {}", e.getMessage());

            PaymentEvent paymentEvent = PaymentEvent.builder()
                    .orderId(orderEvent.getOrderId())
                    .status("PAYMENT_FAILED")
                    .build();

            kafkaTemplate.send("payment-events", paymentEvent);
        }
    }
}