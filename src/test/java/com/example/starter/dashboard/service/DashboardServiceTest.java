package com.example.starter.dashboard.service;

import com.example.starter.common.util.DateRanges;
import com.example.starter.dashboard.dto.DashboardResponse;
import com.example.starter.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Test
    void dailyRevenueIsZeroFilledAndGroupedByVietnamDay() {
        LocalDate today = LocalDate.of(2026, 11, 5);
        // 23:30 Vietnam time on Nov 4 = 16:30 UTC Nov 4 -> belongs to Nov 4, not Nov 5
        var lateEvening = today.minusDays(1).atTime(LocalTime.of(23, 30)).atZone(DateRanges.ZONE).toInstant();
        // 00:15 Vietnam time on Nov 5 = 17:15 UTC Nov 4 -> belongs to Nov 5
        var earlyMorning = today.atTime(LocalTime.of(0, 15)).atZone(DateRanges.ZONE).toInstant();
        when(orderRepository.revenueRows(any(), any())).thenReturn(List.of(
                new Object[]{lateEvening, new BigDecimal("100")},
                new Object[]{earlyMorning, new BigDecimal("50")},
                new Object[]{earlyMorning, new BigDecimal("25")}));

        DashboardService service = new DashboardService(orderRepository, null, null, null, Clock.systemUTC());
        List<DashboardResponse.DailyRevenue> days = service.dailyRevenue(today);

        assertThat(days).hasSize(14);
        assertThat(days.get(0).date()).isEqualTo(today.minusDays(13));
        assertThat(days.get(0).revenue()).isEqualByComparingTo("0");
        assertThat(days.get(12).revenue()).isEqualByComparingTo("100");
        assertThat(days.get(13).revenue()).isEqualByComparingTo("75");
        assertThat(days.get(13).orders()).isEqualTo(2);
    }
}
