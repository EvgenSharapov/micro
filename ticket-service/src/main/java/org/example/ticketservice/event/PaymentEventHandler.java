package org.example.ticketservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commonevents.dto.TicketRequest;
import org.example.commonevents.event.PaymentEvent;
import org.example.commonevents.event.TicketEvent;
import org.example.ticketservice.service.TicketService;
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
                TicketRequest ticketRequest = TicketRequest.builder()
                        .orderId(paymentEvent.getOrderId())
                        .userId(1L)
                        .quantity(1)
                        .build();

                ticketService.createTicket(ticketRequest);

                TicketEvent ticketEvent = TicketEvent.builder()
                        .orderId(paymentEvent.getOrderId())
                        .status("TICKET_CREATED")
                        .build();

                kafkaTemplate.send("ticket-events", ticketEvent);
            } catch (Exception e) {
                log.error("Ticket creation failed: {}", e.getMessage());

                TicketEvent ticketEvent = TicketEvent.builder()
                        .orderId(paymentEvent.getOrderId())
                        .status("TICKET_CREATION_FAILED")
                        .build();

                kafkaTemplate.send("ticket-events", ticketEvent);
            }
        }
    }
}