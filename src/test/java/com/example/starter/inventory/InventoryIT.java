package com.example.starter.inventory;

import com.example.starter.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InventoryIT extends AbstractIntegrationTest {

    private String manager;

    @BeforeEach
    void setUp() throws Exception {
        manager = tokenWithRole("MANAGER");
    }

    private JsonNode receive(long productId, int qty, long unitCost) throws Exception {
        return body(mockMvc.perform(post("/api/v1/stock-receipts").header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("supplierName", "NPP Thành Công",
                                "items", List.of(Map.of("productId", productId, "quantity", qty, "unitCost", unitCost))))))
                .andExpect(status().isCreated())
                .andReturn()).path("data");
    }

    @Test
    void receiptIncreasesStockAndUpdatesMovingAverageCost() throws Exception {
        long p = createProduct(manager, 3000, 1000, 10);

        JsonNode receipt = receive(p, 10, 2000);

        assertThat(receipt.path("code").asText()).matches("^PN\\d{6}-\\d{4}$");
        assertThat(receipt.path("totalAmount").decimalValue()).isEqualByComparingTo("20000");
        JsonNode product = getData("/api/v1/products/" + p, manager);
        assertThat(product.path("quantity").asInt()).isEqualTo(20);
        assertThat(product.path("costPrice").decimalValue()).isEqualByComparingTo("1500");

        JsonNode detail = getData("/api/v1/stock-receipts/" + receipt.path("id").asLong(), manager);
        assertThat(detail.path("items")).hasSize(1);
        assertThat(detail.path("items").get(0).path("quantity").asInt()).isEqualTo(10);
        assertThat(getData("/api/v1/stock-receipts?keyword=" + receipt.path("code").asText(), manager)
                .path("totalElements").asLong()).isEqualTo(1);

        // A product with receipts can no longer be deleted
        mockMvc.perform(delete("/api/v1/products/{id}", p).header(AUTHORIZATION, bearer(manager)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_IN_USE"));
    }

    @Test
    void adjustmentSetsActualQuantityAndIsLogged() throws Exception {
        long p = createProduct(manager, 3000, 1000, 20);

        mockMvc.perform(post("/api/v1/stock-adjustments").header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("productId", p, "actualQuantity", 15, "reason", "Hàng hỏng"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.quantityChange").value(-5))
                .andExpect(jsonPath("$.data.balanceAfter").value(15));

        mockMvc.perform(post("/api/v1/stock-adjustments").header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("productId", p, "actualQuantity", 15, "reason", "again"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("STOCK_ADJUSTMENT_NO_CHANGE"));

        JsonNode movements = getData("/api/v1/stock-movements?productId=" + p + "&sort=id,asc", manager).path("content");
        assertThat(movements).hasSize(2);
        assertThat(movements.get(0).path("type").asText()).isEqualTo("INITIAL");
        assertThat(movements.get(1).path("type").asText()).isEqualTo("ADJUSTMENT");
        assertThat(movements.get(1).path("note").asText()).isEqualTo("Hàng hỏng");
    }

    @Test
    void unknownProductOrDuplicateLinesAreRejected() throws Exception {
        long p = createProduct(manager, 3000, 1000, 1);
        mockMvc.perform(post("/api/v1/stock-receipts").header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("items", List.of(Map.of("productId", 999999, "quantity", 1, "unitCost", 1))))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        mockMvc.perform(post("/api/v1/stock-receipts").header(AUTHORIZATION, bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("items", List.of(
                                Map.of("productId", p, "quantity", 1, "unitCost", 1),
                                Map.of("productId", p, "quantity", 2, "unitCost", 1))))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DUPLICATE_LINE_ITEM"));
    }

    @Test
    void staffCannotAccessInventory() throws Exception {
        String staff = tokenWithRole("STAFF");
        mockMvc.perform(get("/api/v1/stock-receipts").header(AUTHORIZATION, bearer(staff)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/stock-movements").header(AUTHORIZATION, bearer(staff)))
                .andExpect(status().isForbidden());
    }
}
