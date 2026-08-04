package com.orderflow.api.web;

import com.orderflow.api.dto.OrderRequest;
import com.orderflow.api.dto.OrderResponse;
import com.orderflow.api.pubsub.OrderPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderPublisher orderPublisher;

    public OrderController(OrderPublisher orderPublisher) {
        this.orderPublisher = orderPublisher;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        String orderId = orderPublisher.publish(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new OrderResponse(orderId, "PUBLISHED"));
    }
}
