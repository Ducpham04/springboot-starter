package com.example.starter.order.dto;

import com.example.starter.order.entity.OrderStatus;
import com.example.starter.order.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * {@code totalCost}/{@code profit} and item {@code unitCost} are null when the caller lacks
 * PRODUCT_COST_VIEW; {@code items} is null in list responses.
 */
public record OrderResponse(
        Long id,
        String code,
        OrderStatus status,
        PaymentMethod paymentMethod,
        Party customer,
        Party seller,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        BigDecimal totalCost,
        BigDecimal profit,
        BigDecimal cashReceived,
        BigDecimal change,
        String note,
        Instant createdAt,
        Instant cancelledAt,
        String cancelledBy,
        String cancelReason,
        List<Item> items
) {

    public record Party(Long id, String name, String phone) {
    }

    public record Item(Long productId, String sku, String productName, String unit,
                       BigDecimal unitPrice, BigDecimal unitCost, int quantity, BigDecimal lineTotal) {
    }
}
