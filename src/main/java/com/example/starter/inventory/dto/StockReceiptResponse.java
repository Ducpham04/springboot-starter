package com.example.starter.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record StockReceiptResponse(
        Long id,
        String code,
        String supplierName,
        String note,
        BigDecimal totalAmount,
        int itemCount,
        Instant createdAt,
        String createdBy,
        List<Item> items
) {

    public record Item(Long productId, String sku, String productName, String unit,
                       int quantity, BigDecimal unitCost, BigDecimal lineTotal) {
    }
}
