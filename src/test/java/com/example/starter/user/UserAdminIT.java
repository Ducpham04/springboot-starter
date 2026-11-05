package com.example.starter.user;

import com.example.starter.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAdminIT extends AbstractIntegrationTest {

    @Test
    void adminManagesStaffAccounts() throws Exception {
        String admin = adminToken();
        String email = uniqueEmail();
        long id = body(mockMvc.perform(post("/api/v1/users").header(AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "Password1", "fullName", "Lê Thị Hoa",
                                "roles", List.of("STAFF")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roles[0]").value("STAFF"))
                .andReturn()).path("data").path("id").asLong();

        String staffToken = login(email, "Password1").path("accessToken").asText();
        JsonNode me = getData("/api/v1/users/me", staffToken);
        assertThat(me.path("permissions").toString()).contains("ORDER_CREATE").doesNotContain("DASHBOARD_VIEW");

        // Promote to MANAGER: effective immediately for the existing token
        mockMvc.perform(put("/api/v1/users/{id}", id).header(AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Lê Thị Hoa", "roles", List.of("MANAGER")))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/dashboard").header(AUTHORIZATION, bearer(staffToken)))
                .andExpect(status().isOk());

        // Disable: current token rejected, login refused
        mockMvc.perform(patch("/api/v1/users/{id}/status", id).header(AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("enabled", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));
        mockMvc.perform(get("/api/v1/users/me").header(AUTHORIZATION, bearer(staffToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "Password1"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_DISABLED"));

        assertThat(getData("/api/v1/roles", admin)).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void adminCannotDisableOrDemoteSelf() throws Exception {
        String admin = adminToken();
        long selfId = getData("/api/v1/users/me", admin).path("id").asLong();

        mockMvc.perform(patch("/api/v1/users/{id}/status", selfId).header(AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("enabled", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CANNOT_MODIFY_SELF"));
        mockMvc.perform(put("/api/v1/users/{id}", selfId).header(AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Admin", "roles", List.of("STAFF")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CANNOT_MODIFY_SELF"));
    }

    @Test
    void validationAndPermissions() throws Exception {
        mockMvc.perform(post("/api/v1/users").header(AUTHORIZATION, bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", uniqueEmail(), "password", "Password1", "fullName", "X",
                                "roles", List.of("SUPERMAN")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ROLE_NOT_FOUND"));

        String manager = tokenWithRole("MANAGER");
        mockMvc.perform(get("/api/v1/users").header(AUTHORIZATION, bearer(manager)))
                .andExpect(status().isForbidden());
    }
}
