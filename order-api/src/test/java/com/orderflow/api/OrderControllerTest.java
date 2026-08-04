package com.orderflow.api;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PubSubTemplate pubSubTemplate;

    @Test
    void createOrder_publishesAndReturns202() throws Exception {
        ApiFuture<String> future = ApiFutures.immediateFuture("message-id-123");
        when(pubSubTemplate.publish(anyString(), anyString(), any(Map.class))).thenReturn(future);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1","quantity":2,"customerId":"cust-1"}
                                """))
                .andExpect(status().isAccepted());
    }

    @Test
    void createOrder_rejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"","quantity":0,"customerId":""}
                                """))
                .andExpect(status().isBadRequest());
    }
}
