package com.orderflow.worker;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

// Plain unit test (no Spring context) so it doesn't require live GCP credentials to
// build the real Pub/Sub admin clients that full auto-configuration would pull in.
class OrderSubscriberTest {

    @Test
    void start_subscribesToConfiguredSubscription() {
        PubSubTemplate pubSubTemplate = mock(PubSubTemplate.class);
        OrderSubscriber subscriber = new OrderSubscriber(pubSubTemplate, "orders-worker-sub");

        subscriber.start();

        verify(pubSubTemplate).subscribe(eq("orders-worker-sub"), any());
    }
}
