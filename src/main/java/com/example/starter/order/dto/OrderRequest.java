package com.example.starter.order.dto;

import com.example.starter.order.entity.PaymentMethod;
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

/** Prices are always taken from the product on the server, never from the client. */
public record OrderRequest(
        Long customerId,
        @NotEmpty @Size(max = 200) List<@Valid @NotNull Item> items,
        @DecimalMin("0.0") @Digits(integer = 13, fraction = 2) BigDecimal discount,
        @NotNull PaymentMethod paymentMethod,
        @DecimalMin("0.0") @Digits(integer = 13, fraction = 2) BigDecimal cashReceived,
        @Size(max = 500) String note
) {

    public record Item(@NotNull Long productId, @NotNull @Min(1) @Max(100_000) Integer quantity) {
    }
}
