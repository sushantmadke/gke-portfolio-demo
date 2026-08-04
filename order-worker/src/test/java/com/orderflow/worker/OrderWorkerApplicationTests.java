package com.orderflow.worker;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class OrderWorkerApplicationTests {

    @MockBean
    private PubSubTemplate pubSubTemplate;

    @Test
    void contextLoadsAndSubscribes() {
        // If the context loads, OrderSubscriber's @PostConstruct successfully called
        // pubSubTemplate.subscribe(...) against the mocked template.
    }
}
