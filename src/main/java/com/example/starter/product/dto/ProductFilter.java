package com.example.starter.product.dto;

import com.example.starter.product.entity.ProductStatus;

import java.math.BigDecimal;

/**
 * Query parameters for GET /api/v1/products, e.g.
 * {@code ?keyword=sua&categoryId=2&status=ACTIVE&lowStock=true&minPrice=10000&page=0&size=20&sort=name,asc}.
 * Every field is optional.
 */
public record ProductFilter(
        String keyword,
        ProductStatus status,
        Long categoryId,
        Boolean lowStock,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
