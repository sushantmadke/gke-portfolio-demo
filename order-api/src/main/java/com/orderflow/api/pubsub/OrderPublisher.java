package com.orderflow.api.pubsub;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.orderflow.api.dto.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Publishes accepted orders to the orders topic. Each message carries an idempotency
 * key attribute so order-worker can safely dedupe on redelivery.
 */
@Component
public class OrderPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderPublisher.class);

    private final PubSubTemplate pubSubTemplate;
    private final String ordersTopic;

    public OrderPublisher(PubSubTemplate pubSubTemplate,
                           @Value("${orderflow.pubsub.orders-topic}") String ordersTopic) {
        this.pubSubTemplate = pubSubTemplate;
        this.ordersTopic = ordersTopic;
    }

    public String publish(OrderRequest order) {
        String orderId = UUID.randomUUID().toString();
        String payload = "{\"orderId\":\"%s\",\"sku\":\"%s\",\"quantity\":%d,\"customerId\":\"%s\"}"
                .formatted(orderId, order.sku(), order.quantity(), order.customerId());

        pubSubTemplate.publish(ordersTopic, payload, Map.of(
                "orderId", orderId,
                "idempotencyKey", orderId
        ));

        log.info("Published order {} to topic {}", orderId, ordersTopic);
        return orderId;
    }
}
