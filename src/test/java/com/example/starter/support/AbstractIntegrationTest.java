package com.example.starter.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for integration tests: full Spring context + real MySQL (Testcontainers) + MockMvc.
 * Extend it and name the class *IT so it runs in the Maven `verify` phase.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, TestMailConfiguration.class})
public abstract class AbstractIntegrationTest {

    protected static final String ADMIN_EMAIL = "admin@test.local";
    protected static final String ADMIN_PASSWORD = "Admin@123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected CapturingMailService mailService;

    protected String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    protected JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }

    /** Logs in and returns the "data" node (accessToken, refreshToken, user). */
    protected JsonNode login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return body(result).path("data");
    }

    protected String adminToken() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD).path("accessToken").asText();
    }

    /** Registers a fresh USER account and returns the "data" node. */
    protected JsonNode registerUser(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password, "fullName", "Test User"))))
                .andExpect(status().isCreated())
                .andReturn();
        return body(result).path("data");
    }

    protected String userToken() throws Exception {
        return registerUser(uniqueEmail(), "Password1").path("accessToken").asText();
    }

    /** Creates a user with the given role through the admin API and returns its access token. */
    protected String tokenWithRole(String role) throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "Password1", "fullName", role + " test",
                                "roles", java.util.List.of(role)))))
                .andExpect(status().isCreated());
        return login(email, "Password1").path("accessToken").asText();
    }

    /** Creates an ACTIVE product with opening stock and returns its id. */
    protected long createProduct(String token, long price, long cost, int quantity) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("sku", "T-" + UUID.randomUUID().toString().substring(0, 8));
        body.put("name", "Sản phẩm test " + UUID.randomUUID().toString().substring(0, 4));
        body.put("price", price);
        body.put("costPrice", cost);
        body.put("quantity", quantity);
        body.put("minStock", 2);
        body.put("unit", "cái");
        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return body(result).path("data").path("id").asLong();
    }

    protected JsonNode getData(String url, String token) throws Exception {
        return body(mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()).path("data");
    }

    protected static String uniqueEmail() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }
}
