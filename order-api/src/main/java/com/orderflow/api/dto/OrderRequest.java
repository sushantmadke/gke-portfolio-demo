package com.orderflow.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderRequest(
        @NotBlank String sku,
        @Min(1) int quantity,
        @NotBlank String customerId
) {
}
