package com.example.starter.dashboard.dto;

import com.example.starter.order.dto.OrderResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        BigDecimal todayRevenue,
        long todayOrders,
        BigDecimal monthRevenue,
        long monthOrders,
        BigDecimal monthProfit,
        long lowStockCount,
        long customerCount,
        List<DailyRevenue> dailyRevenue,
        List<TopProduct> topProducts,
        List<LowStockProduct> lowStockProducts,
        List<OrderResponse> recentOrders
) {

    public record DailyRevenue(LocalDate date, BigDecimal revenue, long orders) {
    }

    public record TopProduct(Long productId, String name, long quantity, BigDecimal revenue) {
    }

    public record LowStockProduct(Long id, String sku, String name, String unit, int quantity, int minStock) {
    }
}
