package com.example.starter.product.dto;

import com.example.starter.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** {@code costPrice} is null when the caller lacks PRODUCT_COST_VIEW. */
public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        Long categoryId,
        String categoryName,
        String unit,
        BigDecimal price,
        BigDecimal costPrice,
        Integer quantity,
        Integer minStock,
        boolean lowStock,
        String imageUrl,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {

    public ProductResponse withoutCost() {
        return new ProductResponse(id, sku, name, description, categoryId, categoryName, unit, price, null,
                quantity, minStock, lowStock, imageUrl, status, createdAt, updatedAt, createdBy, updatedBy);
    }
}
