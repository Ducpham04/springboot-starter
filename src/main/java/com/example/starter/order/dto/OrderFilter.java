package com.example.starter.order.dto;

import com.example.starter.order.entity.OrderStatus;

import java.time.LocalDate;

/** GET /api/v1/orders?from=2026-11-01&to=2026-11-05&status=COMPLETED&keyword=DH2611&customerId=3 */
public record OrderFilter(LocalDate from, LocalDate to, OrderStatus status, String keyword,
                          Long customerId, Long sellerId) {
}
