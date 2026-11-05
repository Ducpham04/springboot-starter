package com.example.starter.dashboard.service;

import com.example.starter.common.util.DateRanges;
import com.example.starter.customer.repository.CustomerRepository;
import com.example.starter.dashboard.dto.DashboardResponse;
import com.example.starter.order.dto.OrderFilter;
import com.example.starter.order.service.OrderService;
import com.example.starter.order.repository.OrderRepository;
import com.example.starter.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    static final int CHART_DAYS = 14;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderService orderService;
    private final Clock clock;

    public DashboardResponse summary() {
        LocalDate today = LocalDate.now(clock.withZone(DateRanges.ZONE));
        Instant todayStart = DateRanges.startOfDay(today);
        Instant tomorrowStart = DateRanges.startOfDay(today.plusDays(1));
        Instant monthStart = DateRanges.startOfDay(today.withDayOfMonth(1));

        Object[] todayTotals = orderRepository.totals(todayStart, tomorrowStart).get(0);
        Object[] monthTotals = orderRepository.totals(monthStart, tomorrowStart).get(0);
        BigDecimal monthRevenue = (BigDecimal) monthTotals[1];
        BigDecimal monthCost = (BigDecimal) monthTotals[2];

        List<DashboardResponse.TopProduct> top = orderRepository
                .topProducts(monthStart, tomorrowStart, PageRequest.of(0, 5)).stream()
                .map(r -> new DashboardResponse.TopProduct((Long) r[0], (String) r[1],
                        ((Number) r[2]).longValue(), (BigDecimal) r[3]))
                .toList();

        List<DashboardResponse.LowStockProduct> lowStock = productRepository.findLowStock(PageRequest.of(0, 8))
                .stream()
                .map(p -> new DashboardResponse.LowStockProduct(p.getId(), p.getSku(), p.getName(), p.getUnit(),
                        p.getQuantity(), p.getMinStock()))
                .toList();

        var recent = orderService.search(new OrderFilter(null, null, null, null, null, null),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))).content();

        return new DashboardResponse(
                (BigDecimal) todayTotals[1], (Long) todayTotals[0],
                monthRevenue, (Long) monthTotals[0], monthRevenue.subtract(monthCost),
                productRepository.countLowStock(), customerRepository.count(),
                dailyRevenue(today), top, lowStock, recent);
    }

    /** Revenue per business day (Vietnam time) for the last {@value CHART_DAYS} days, zero-filled. */
    List<DashboardResponse.DailyRevenue> dailyRevenue(LocalDate today) {
        LocalDate first = today.minusDays(CHART_DAYS - 1L);
        Map<LocalDate, BigDecimal> revenue = new LinkedHashMap<>();
        Map<LocalDate, Long> count = new LinkedHashMap<>();
        for (LocalDate d = first; !d.isAfter(today); d = d.plusDays(1)) {
            revenue.put(d, BigDecimal.ZERO);
            count.put(d, 0L);
        }
        for (Object[] row : orderRepository.revenueRows(DateRanges.startOfDay(first),
                DateRanges.startOfDay(today.plusDays(1)))) {
            LocalDate day = ((Instant) row[0]).atZone(DateRanges.ZONE).toLocalDate();
            revenue.merge(day, (BigDecimal) row[1], BigDecimal::add);
            count.merge(day, 1L, Long::sum);
        }
        List<DashboardResponse.DailyRevenue> result = new ArrayList<>();
        revenue.forEach((d, v) -> result.add(new DashboardResponse.DailyRevenue(d, v, count.get(d))));
        return result;
    }
}
