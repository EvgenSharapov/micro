package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderResponse placeOrder(OrderRequest orderRequest) {
        Order order = Order.builder()
                .userId(orderRequest.getUserId())
                .ticketId(orderRequest.getTicketId())
                .quantity(orderRequest.getQuantity())
                .status("PENDING")
                .build();

        Order savedOrder = orderRepository.save(order);

        // Отправка события в Kafka
        OrderEvent event = OrderEvent.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .ticketId(savedOrder.getTicketId())
                .quantity(savedOrder.getQuantity())
                .status(savedOrder.getStatus())
                .build();

        kafkaTemplate.send("order-events", event);

        return mapToOrderResponse(savedOrder);
    }

    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToOrderResponse(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .ticketId(order.getTicketId())
                .quantity(order.getQuantity())
                .status(order.getStatus())
                .build();
    }
}