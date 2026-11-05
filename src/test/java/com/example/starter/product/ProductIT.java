package com.example.starter.product;

import com.example.starter.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductIT extends AbstractIntegrationTest {

    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        admin = adminToken();
    }

    private Map<String, Object> product(String sku, String name, double price, String category) {
        Map<String, Object> body = new HashMap<>();
        body.put("sku", sku);
        body.put("name", name);
        body.put("price", price);
        body.put("quantity", 10);
        if (category != null) {
            body.put("categoryId", Long.valueOf(category));
        }
        return body;
    }

    private long create(Map<String, Object> body) throws Exception {
        return body(mockMvc.perform(post("/api/v1/products")
                        .header(AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn()).path("data").path("id").asLong();
    }

    private static String sku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void fullCrudLifecycle() throws Exception {
        String sku = sku();
        long id = create(product(sku, "Laptop", 1500.5, null));

        mockMvc.perform(get("/api/v1/products/{id}", id).header(AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value(sku))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.createdBy").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.data.createdAt").exists());

        Map<String, Object> update = product(sku, "Laptop Pro", 1999, null);
        update.put("status", "INACTIVE");
        mockMvc.perform(put("/api/v1/products/{id}", id)
                        .header(AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Laptop Pro"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(jsonPath("$.data.updatedBy").value(ADMIN_EMAIL));

        mockMvc.perform(delete("/api/v1/products/{id}", id).header(AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/products/{id}", id).header(AUTHORIZATION, bearer(admin))
                        .header(ACCEPT_LANGUAGE, "vi"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy sản phẩm có id " + id));
    }

    @Test
    void duplicateSkuReturnsConflict() throws Exception {
        String sku = sku();
        create(product(sku, "A", 1, null));
        mockMvc.perform(post("/api/v1/products")
                        .header(AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(product(sku, "B", 2, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_SKU_EXISTS"));
    }

    @Test
    void invalidBodyReturnsFieldErrors() throws Exception {
        Map<String, Object> body = product("bad sku!", "", -1, null);
        mockMvc.perform(post("/api/v1/products")
                        .header(AUTHORIZATION, bearer(admin))
                        .header(ACCEPT_LANGUAGE, "vi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("sku")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("price")))
                .andExpect(jsonPath("$.errors[*].message").value(hasItem("SKU chỉ được chứa chữ, số, '-' và '_'")));
    }

    @Test
    void searchWithFiltersPagingAndSorting() throws Exception {
        String tag = "tag" + UUID.randomUUID().toString().substring(0, 6);
        create(product(sku(), "Alpha phone " + tag, 100, null));
        create(product(sku(), "Beta phone " + tag, 200, null));
        create(product(sku(), "Gamma tablet " + tag, 300, null));

        mockMvc.perform(get("/api/v1/products")
                        .header(AUTHORIZATION, bearer(admin))
                        .param("keyword", tag)
                        .param("minPrice", "150")
                        .param("maxPrice", "250")
                        .param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Beta phone " + tag));

        mockMvc.perform(get("/api/v1/products")
                        .header(AUTHORIZATION, bearer(admin))
                        .param("keyword", tag)
                        .param("size", "2")
                        .param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[*].price").value(everyItem(is(greaterThanOrEqualTo(100.0)))))
                .andExpect(jsonPath("$.data.content[0].name").value("Alpha phone " + tag));
    }

    @Test
    void invalidSortPropertyReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products").header(AUTHORIZATION, bearer(admin)).param("sort", "unknownField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void regularUserCanReadButNotWrite() throws Exception {
        String user = userToken();
        mockMvc.perform(get("/api/v1/products").header(AUTHORIZATION, bearer(user)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/products")
                        .header(AUTHORIZATION, bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(product(sku(), "X", 1, null))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }
}
