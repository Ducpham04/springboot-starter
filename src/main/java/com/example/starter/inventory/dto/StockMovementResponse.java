package com.example.starter.inventory.dto;

import com.example.starter.inventory.entity.MovementType;

import java.time.Instant;

public record StockMovementResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        MovementType type,
        int quantityChange,
        int balanceAfter,
        String referenceCode,
        String note,
        Instant createdAt,
        String createdBy
) {
}
