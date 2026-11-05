package com.example.starter.product.dto;

import com.example.starter.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Query parameters for GET /api/v1/products, e.g.
 * {@code ?keyword=phone&status=ACTIVE&category=a&category=b&minPrice=10&maxPrice=100&page=0&size=20&sort=price,desc}.
 * Every field is optional.
 */
public record ProductFilter(
        String keyword,
        ProductStatus status,
        List<String> category,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
