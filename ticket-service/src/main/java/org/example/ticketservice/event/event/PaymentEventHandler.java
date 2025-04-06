package org.example.ticketservice.event.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {
    private final TicketService ticketService;
    private final KafkaTemplate<String, TicketEvent> kafkaTemplate;

    @KafkaListener(topics = "payment-events", groupId = "ticket-group")
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        log.info("Received payment event: {}", paymentEvent);

        if ("PAYMENT_COMPLETED".equals(paymentEvent.getStatus())) {
            try {
                // Здесь должна быть логика получения информации о заказе
                // Для упрощения используем фиктивные данные
                TicketRequest ticketRequest = TicketRequest.builder()
                        .orderId(paymentEvent.getOrderId())
                        .userId(1L) // Фиктивный userId
                        .quantity(1) // Фиктивное количество
                        .build();

                ticketService.createTicket(ticketRequest);

                // Отправка успешного события
                TicketEvent ticketEvent = TicketEvent.builder()
                        .orderId(paymentEvent.getOrderId())
                        .status("TICKET_CREATED")
                        .build();

                kafkaTemplate.send("ticket-events", ticketEvent);
            } catch (Exception e) {
                log.error("Ticket creation failed: {}", e.getMessage());

                // Отправка события об ошибке
                TicketEvent ticketEvent = TicketEvent.builder()
                        .orderId(paymentEvent.getOrderId())
                        .status("TICKET_CREATION_FAILED")
                        .build();

                kafkaTemplate.send("ticket-events", ticketEvent);
            }
        }
    }
}