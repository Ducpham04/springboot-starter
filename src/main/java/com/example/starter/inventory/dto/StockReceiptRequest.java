package com.example.starter.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record StockReceiptRequest(
        @Size(max = 200) String supplierName,
        @Size(max = 500) String note,
        @NotEmpty @Size(max = 200) List<@Valid @NotNull Item> items
) {

    public record Item(
            @NotNull Long productId,
            @NotNull @Min(1) @Max(1_000_000) Integer quantity,
            @NotNull @DecimalMin("0.0") @Digits(integer = 13, fraction = 2) BigDecimal unitCost
    ) {
    }
}
