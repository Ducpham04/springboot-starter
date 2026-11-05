package com.example.starter.order;

import com.example.starter.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Main business flow: sell at the counter, stock decreases, visibility rules, cancellation. */
class OrderIT extends AbstractIntegrationTest {

    private String manager;
    private String staff;

    @BeforeEach
    void setUp() throws Exception {
        manager = tokenWithRole("MANAGER");
        staff = tokenWithRole("STAFF");
    }

    private Map<String, Object> order(long productId, int qty) {
        return Map.of("items", List.of(Map.of("productId", productId, "quantity", qty)), "paymentMethod", "TRANSFER");
    }

    private JsonNode createOrder(String token, Object body) throws Exception {
        return body(mockMvc.perform(post("/api/v1/orders").header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isCreated())
                .andReturn()).path("data");
    }

    private int stock(long productId) throws Exception {
        return getData("/api/v1/products/" + productId, manager).path("quantity").asInt();
    }

    @Test
    void staffSellsAndStockDecreases() throws Exception {
        long p1 = createProduct(manager, 15000, 10000, 10);
        long p2 = createProduct(manager, 4500, 3600, 50);

        JsonNode order = createOrder(staff, Map.of(
                "items", List.of(Map.of("productId", p1, "quantity", 3), Map.of("productId", p2, "quantity", 2)),
                "paymentMethod", "CASH", "cashReceived", 100000, "discount", 4000));

        assertThat(order.path("code").asText()).matches("^DH\\d{6}-\\d{4}$");
        assertThat(order.path("subtotal").decimalValue()).isEqualByComparingTo("54000");
        assertThat(order.path("total").decimalValue()).isEqualByComparingTo("50000");
        assertThat(order.path("change").decimalValue()).isEqualByComparingTo("50000");
        assertThat(order.path("profit").isMissingNode()).as("staff must not see profit").isTrue();
        assertThat(order.path("items")).hasSize(2);
        assertThat(stock(p1)).isEqualTo(7);
        assertThat(stock(p2)).isEqualTo(48);

        JsonNode movements = getData("/api/v1/stock-movements?productId=" + p1, manager).path("content");
        assertThat(movements.get(0).path("type").asText()).isEqualTo("SALE");
        assertThat(movements.get(0).path("quantityChange").asInt()).isEqualTo(-3);
        assertThat(movements.get(0).path("referenceCode").asText()).isEqualTo(order.path("code").asText());

        // Manager sees cost and profit: (15000-10000)*3 + (4500-3600)*2 - 4000 discount
        JsonNode detail = getData("/api/v1/orders/" + order.path("id").asLong(), manager);
        assertThat(detail.path("profit").decimalValue()).isEqualByComparingTo("12800");
    }

    @Test
    void cannotSellMoreThanStock() throws Exception {
        long p = createProduct(manager, 10000, 8000, 2);

        mockMvc.perform(post("/api/v1/orders").header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON).content(json(order(p, 3))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.message", matchesPattern(".*chỉ còn 2.*")));
        assertThat(stock(p)).isEqualTo(2);
    }

    @Test
    void rejectsInvalidAmounts() throws Exception {
        long p = createProduct(manager, 10000, 8000, 10);

        mockMvc.perform(post("/api/v1/orders").header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("items", List.of(Map.of("productId", p, "quantity", 1)),
                                "paymentMethod", "CASH", "discount", 20000))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DISCOUNT_TOO_LARGE"));

        mockMvc.perform(post("/api/v1/orders").header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("items", List.of(Map.of("productId", p, "quantity", 1)),
                                "paymentMethod", "CASH", "cashReceived", 5000))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CASH_NOT_ENOUGH"));

        mockMvc.perform(post("/api/v1/orders").header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("items", List.of(), "paymentMethod", "CASH"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        assertThat(stock(p)).isEqualTo(10);
    }

    @Test
    void inactiveProductCannotBeSold() throws Exception {
        long p = createProduct(manager, 10000, 8000, 10);
        JsonNode product = getData("/api/v1/products/" + p, manager);
        mockMvc.perform(put("/api/v1/products/{id}", p).header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("sku", product.path("sku").asText(), "name", "X", "price", 10000,
                                "status", "INACTIVE"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/orders").header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON).content(json(order(p, 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PRODUCT_INACTIVE"));
    }

    @Test
    void staffOnlySeesOwnOrders() throws Exception {
        long p = createProduct(manager, 10000, 8000, 10);
        long id = createOrder(staff, order(p, 1)).path("id").asLong();
        String otherStaff = tokenWithRole("STAFF");

        mockMvc.perform(get("/api/v1/orders/{id}", id).header(AUTHORIZATION, bearer(otherStaff)))
                .andExpect(status().isNotFound());
        assertThat(getData("/api/v1/orders", otherStaff).path("totalElements").asLong()).isZero();
        assertThat(getData("/api/v1/orders", staff).path("totalElements").asLong()).isEqualTo(1);
        mockMvc.perform(get("/api/v1/orders/{id}", id).header(AUTHORIZATION, bearer(manager)))
                .andExpect(status().isOk());
    }

    @Test
    void cancelRestoresStockOnlyOnce() throws Exception {
        long p = createProduct(manager, 10000, 8000, 10);
        long id = createOrder(staff, order(p, 4)).path("id").asLong();
        assertThat(stock(p)).isEqualTo(6);

        mockMvc.perform(post("/api/v1/orders/{id}/cancel", id).header(AUTHORIZATION, bearer(staff))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("reason", "x"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/orders/{id}/cancel", id).header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("reason", "Khách đổi ý"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancelReason").value("Khách đổi ý"));
        assertThat(stock(p)).isEqualTo(10);

        mockMvc.perform(post("/api/v1/orders/{id}/cancel", id).header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("reason", "again"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_ALREADY_CANCELLED"));
        assertThat(stock(p)).isEqualTo(10);
    }

    @Test
    void concurrentSalesNeverOversell() throws Exception {
        long p = createProduct(manager, 10000, 8000, 3);
        ExecutorService pool = Executors.newFixedThreadPool(6);
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                tasks.add(() -> mockMvc.perform(post("/api/v1/orders").header(AUTHORIZATION, bearer(staff))
                                .contentType(MediaType.APPLICATION_JSON).content(json(order(p, 1))))
                        .andReturn().getResponse().getStatus());
            }
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> f : pool.invokeAll(tasks)) {
                statuses.add(f.get());
            }
            assertThat(statuses).filteredOn(s -> s == 201).hasSize(3);
            assertThat(statuses).filteredOn(s -> s == 409).hasSize(3);
        } finally {
            pool.shutdown();
        }
        assertThat(stock(p)).isZero();
    }
}
