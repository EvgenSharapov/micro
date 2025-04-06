package org.example.paymentservice.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            // Попытка обработки платежа
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .orderId(orderEvent.getOrderId())
                    .userId(orderEvent.getUserId())
                    .amount(orderEvent.getQuantity() * 100) // Упрощенная логика расчета суммы
                    .build();

            paymentService.processPayment(paymentRequest);

            // Отправка успешного события
            PaymentEvent paymentEvent = PaymentEvent.builder()
                    .orderId(orderEvent.getOrderId())
                    .status("PAYMENT_COMPLETED")
                    .build();

            kafkaTemplate.send("payment-events", paymentEvent);
        } catch (Exception e) {
            log.error("Payment processing failed: {}", e.getMessage());

            // Отправка события об ошибке
            PaymentEvent paymentEvent = PaymentEvent.builder()
                    .orderId(orderEvent.getOrderId())
                    .status("PAYMENT_FAILED")
                    .build();

            kafkaTemplate.send("payment-events", paymentEvent);
        }
    }
}