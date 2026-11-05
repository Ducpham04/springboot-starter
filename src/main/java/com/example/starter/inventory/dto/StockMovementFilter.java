package com.example.starter.inventory.dto;

import com.example.starter.inventory.entity.MovementType;

public record StockMovementFilter(Long productId, MovementType type, String keyword) {
}
