package com.orderflow.api;

import com.orderflow.api.dto.OrderRequest;
import com.orderflow.api.pubsub.OrderPublisher;
import com.orderflow.api.web.OrderController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest loads only the web layer, not the full application context - this
// avoids pulling in the real Pub/Sub auto-configuration (which needs live GCP
// credentials) just to test the HTTP contract of OrderController.
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderPublisher orderPublisher;

    @Test
    void createOrder_publishesAndReturns202() throws Exception {
        when(orderPublisher.publish(any(OrderRequest.class))).thenReturn("order-id-123");

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
