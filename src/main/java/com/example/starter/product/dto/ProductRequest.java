package com.example.starter.product.dto;

import com.example.starter.product.entity.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Body for create (POST) and update (PUT).
 * {@code quantity} is the opening stock and is only used on create; afterwards stock changes
 * exclusively through stock documents (see inventory module).
 */
public record ProductRequest(
        @NotBlank @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "{validation.product.sku.pattern}") String sku,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 5000) String description,
        Long categoryId,
        @Size(max = 30) String unit,
        @NotNull @DecimalMin("0.0") @Digits(integer = 13, fraction = 2) BigDecimal price,
        @DecimalMin("0.0") @Digits(integer = 13, fraction = 2) BigDecimal costPrice,
        @Min(0) Integer quantity,
        @Min(0) Integer minStock,
        @Size(max = 500) String imageUrl,
        ProductStatus status
) {
}
