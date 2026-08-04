package com.orderflow.worker;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Subscribes to the orders subscription with manual ack/nack. Transient failures are
 * nacked for redelivery; after the subscription's maxDeliveryAttempts (set in Terraform,
 * see infra/pubsub.tf) Pub/Sub routes the message to the orders-dlq topic instead of
 * retrying forever. Unrecoverable failures are acked to avoid a poison-pill loop.
 */
@Component
public class OrderSubscriber {

    private static final Logger log = LoggerFactory.getLogger(OrderSubscriber.class);

    private final PubSubTemplate pubSubTemplate;
    private final String subscription;

    public OrderSubscriber(PubSubTemplate pubSubTemplate,
                            @Value("${orderflow.pubsub.orders-subscription}") String subscription) {
        this.pubSubTemplate = pubSubTemplate;
        this.subscription = subscription;
    }

    @PostConstruct
    void start() {
        pubSubTemplate.subscribe(subscription, this::handle);
        log.info("Subscribed to {}", subscription);
    }

    private void handle(BasicAcknowledgeablePubsubMessage message) {
        String orderId = message.getPubsubMessage().getAttributesMap().getOrDefault("orderId", "unknown");
        try {
            simulateProcessing();
            message.ack();
            log.info("Processed and acked order {}", orderId);
        } catch (TransientProcessingException e) {
            log.warn("Transient failure processing order {}, nacking for redelivery: {}", orderId, e.getMessage());
            message.nack();
        } catch (Exception e) {
            log.error("Unrecoverable error processing order {}, acking to avoid poison-pill redelivery", orderId, e);
            message.ack();
        }
    }

    /** Variable-latency simulated work so load tests produce a visible, gradual scale-out. */
    private void simulateProcessing() throws InterruptedException, TransientProcessingException {
        Thread.sleep(200 + ThreadLocalRandom.current().nextInt(800));
        if (ThreadLocalRandom.current().nextInt(20) == 0) {
            throw new TransientProcessingException("simulated downstream timeout");
        }
    }
}
