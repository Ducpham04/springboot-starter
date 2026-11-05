package com.example.starter.catalog;

import com.example.starter.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Categories and customers. */
class CatalogIT extends AbstractIntegrationTest {

    private String manager;

    @BeforeEach
    void setUp() throws Exception {
        manager = tokenWithRole("MANAGER");
    }

    private static String rnd() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    private static String phone() {
        return "09" + String.format("%08d", (long) (Math.random() * 100_000_000L));
    }

    @Test
    void categoryLifecycle() throws Exception {
        String name = "Đồ uống " + rnd();
        long id = body(mockMvc.perform(post("/api/v1/categories").header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", name))))
                .andExpect(status().isCreated()).andReturn()).path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/categories").header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", name.toUpperCase()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_NAME_EXISTS"));

        Map<String, Object> product = new HashMap<>();
        product.put("sku", "C-" + rnd());
        product.put("name", "Trà đá");
        product.put("price", 5000);
        product.put("categoryId", id);
        mockMvc.perform(post("/api/v1/products").header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON).content(json(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.categoryName").value(name));

        JsonNode categories = getData("/api/v1/categories", manager);
        JsonNode created = null;
        for (JsonNode c : categories) {
            if (c.path("id").asLong() == id) {
                created = c;
            }
        }
        assertThat(created).isNotNull();
        assertThat(created.path("productCount").asLong()).isEqualTo(1);

        mockMvc.perform(delete("/api/v1/categories/{id}", id).header(AUTHORIZATION, bearer(manager)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_IN_USE"));

        mockMvc.perform(post("/api/v1/categories").header(AUTHORIZATION, bearer(tokenWithRole("STAFF")))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", "x" + rnd()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerLifecycleWithStats() throws Exception {
        String staff = tokenWithRole("STAFF");
        String phone = phone();
        long id = body(mockMvc.perform(post("/api/v1/customers").header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Nguyễn Văn An", "phone", phone))))
                .andExpect(status().isCreated()).andReturn()).path("data").path("id").asLong();

        // Same number written with +84 is a duplicate
        mockMvc.perform(post("/api/v1/customers").header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Khác", "phone", "+84" + phone.substring(1)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_PHONE_EXISTS"));

        mockMvc.perform(post("/api/v1/customers").header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Sai số", "phone", "12345"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value("Số điện thoại không hợp lệ"));

        long p = createProduct(manager, 20000, 15000, 10);
        mockMvc.perform(post("/api/v1/orders").header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("customerId", id, "paymentMethod", "CARD",
                                "items", List.of(Map.of("productId", p, "quantity", 2))))))
                .andExpect(status().isCreated());

        JsonNode detail = getData("/api/v1/customers/" + id, staff);
        assertThat(detail.path("orderCount").asLong()).isEqualTo(1);
        assertThat(detail.path("totalSpent").decimalValue()).isEqualByComparingTo("40000");
        assertThat(detail.path("recentOrders")).hasSize(1);
        assertThat(getData("/api/v1/customers?keyword=" + phone, staff).path("totalElements").asLong()).isEqualTo(1);

        mockMvc.perform(put("/api/v1/customers/{id}", id).header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Nguyễn Văn An (VIP)", "phone", phone))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Nguyễn Văn An (VIP)"));

        mockMvc.perform(delete("/api/v1/customers/{id}", id).header(AUTHORIZATION, bearer(staff)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/customers/{id}", id).header(AUTHORIZATION, bearer(manager)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_IN_USE"));
    }
}
