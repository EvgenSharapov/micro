package org.example.ticketservice.service;

import org.example.commonevents.dto.TicketRequest;
import org.example.commonevents.dto.TicketResponse;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
    public TicketResponse createTicket(TicketRequest ticketRequest) {
        return TicketResponse.builder()
                .ticketId("TICKET-" + System.currentTimeMillis())
                .orderId(ticketRequest.getOrderId())
                .userId(ticketRequest.getUserId())
                .status("CREATED")
                .build();
    }
}