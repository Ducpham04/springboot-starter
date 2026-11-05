package com.example.starter.dashboard;

import com.example.starter.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardIT extends AbstractIntegrationTest {

    @Test
    void reflectsTodaySalesAndRequiresPermission() throws Exception {
        String manager = tokenWithRole("MANAGER");
        JsonNode before = getData("/api/v1/dashboard", manager);
        assertThat(before.path("dailyRevenue")).hasSize(14);

        long p = createProduct(manager, 50000, 30000, 5);
        mockMvc.perform(post("/api/v1/orders").header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("paymentMethod", "TRANSFER",
                                "items", List.of(Map.of("productId", p, "quantity", 4))))))
                .andExpect(status().isCreated());

        JsonNode after = getData("/api/v1/dashboard", manager);
        assertThat(after.path("todayOrders").asLong()).isEqualTo(before.path("todayOrders").asLong() + 1);
        assertThat(after.path("todayRevenue").decimalValue().subtract(before.path("todayRevenue").decimalValue()))
                .isEqualByComparingTo("200000");
        assertThat(after.path("monthProfit").decimalValue().subtract(before.path("monthProfit").decimalValue()))
                .isEqualByComparingTo("80000");
        JsonNode today = after.path("dailyRevenue").get(13);
        assertThat(today.path("orders").asLong()).isGreaterThanOrEqualTo(1);
        // stock 5 - 4 = 1 <= minStock 2 -> one more low-stock product
        assertThat(after.path("lowStockCount").asLong()).isEqualTo(before.path("lowStockCount").asLong() + 1);
        assertThat(after.path("recentOrders").get(0).path("total").decimalValue()).isEqualByComparingTo("200000");

        mockMvc.perform(get("/api/v1/dashboard").header(AUTHORIZATION, bearer(tokenWithRole("STAFF"))))
                .andExpect(status().isForbidden());
    }
}
