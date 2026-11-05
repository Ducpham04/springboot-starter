package com.example.starter.inventory.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Stock count: sets the actual quantity on hand; the difference is recorded as an ADJUSTMENT. */
public record StockAdjustmentRequest(
        @NotNull Long productId,
        @NotNull @Min(0) @Max(100_000_000) Integer actualQuantity,
        @NotBlank @Size(max = 500) String reason
) {
}
