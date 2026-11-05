package com.example.starter.customer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** {@code recentOrders} is only filled by GET /customers/{id}. */
public record CustomerResponse(
        Long id,
        String name,
        String phone,
        String email,
        String address,
        String note,
        long orderCount,
        BigDecimal totalSpent,
        Instant lastOrderAt,
        Instant createdAt,
        List<RecentOrder> recentOrders
) {

    public record RecentOrder(Long id, String code, BigDecimal total, String status, Instant createdAt) {
    }
}
