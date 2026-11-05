package com.example.starter.product.dto;

import com.example.starter.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        String category,
        String imageUrl,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
