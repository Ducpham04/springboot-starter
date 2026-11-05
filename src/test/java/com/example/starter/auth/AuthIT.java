package com.example.starter.auth;

import com.example.starter.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIT extends AbstractIntegrationTest {

    @Test
    void registerThenLoginAndReadProfile() throws Exception {
        String email = uniqueEmail();
        JsonNode registered = registerUser(email, "Password1");
        assertThat(registered.path("refreshToken").asText()).isNotBlank();
        assertThat(registered.path("user").path("roles").toString()).contains("USER");

        String token = login(email, "Password1").path("accessToken").asText();
        mockMvc.perform(get("/api/v1/users/me").header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    void registerWithDuplicateEmailReturnsConflict() throws Exception {
        String email = uniqueEmail();
        registerUser(email, "Password1");
        mockMvc.perform(post("/api/v1/auth/register")
                        .header(ACCEPT_LANGUAGE, "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "Password1", "fullName", "X"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Email " + email + " is already registered"));
    }

    @Test
    void validationMessagesFollowAcceptLanguage() throws Exception {
        String invalid = json(Map.of("email", "not-an-email", "password", "short", "fullName", ""));

        mockMvc.perform(post("/api/v1/auth/register").header(ACCEPT_LANGUAGE, "vi")
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Dữ liệu không hợp lệ"))
                .andExpect(jsonPath("$.errors[*].message").value(hasItem("email không đúng định dạng")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("fullName")));

        mockMvc.perform(post("/api/v1/auth/register").header(ACCEPT_LANGUAGE, "en")
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input data"))
                .andExpect(jsonPath("$.errors[*].message").value(hasItem("must be a well-formed email address")));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", ADMIN_EMAIL, "password", "wrong-password1"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Email hoặc mật khẩu không đúng"));
    }

    @Test
    void requestWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/users/me").header(AUTHORIZATION, "Bearer garbage"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
    }

    @Test
    void refreshRotatesTokenAndDetectsReuse() throws Exception {
        JsonNode tokens = registerUser(uniqueEmail(), "Password1");
        String refresh1 = tokens.path("refreshToken").asText();

        JsonNode refreshed = body(mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refresh1))))
                .andExpect(status().isOk())
                .andReturn()).path("data");
        String refresh2 = refreshed.path("refreshToken").asText();
        assertThat(refresh2).isNotEqualTo(refresh1);

        // Re-using the rotated token is rejected and revokes the whole family
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refresh1))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_INVALID"));
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refresh2))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutBlacklistsAccessTokenAndRevokesRefreshToken() throws Exception {
        JsonNode tokens = registerUser(uniqueEmail(), "Password1");
        String access = tokens.path("accessToken").asText();
        String refresh = tokens.path("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(AUTHORIZATION, bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refresh))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công"));

        mockMvc.perform(get("/api/v1/users/me").header(AUTHORIZATION, bearer(access)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refresh))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordInvalidatesExistingTokens() throws Exception {
        String email = uniqueEmail();
        JsonNode tokens = registerUser(email, "Password1");
        String access = tokens.path("accessToken").asText();

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header(AUTHORIZATION, bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("oldPassword", "WrongOld1", "newPassword", "NewPassword2"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OLD_PASSWORD_INCORRECT"));

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header(AUTHORIZATION, bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("oldPassword", "Password1", "newPassword", "NewPassword2"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/me").header(AUTHORIZATION, bearer(access)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", tokens.path("refreshToken").asText()))))
                .andExpect(status().isUnauthorized());

        String newAccess = login(email, "NewPassword2").path("accessToken").asText();
        mockMvc.perform(get("/api/v1/users/me").header(AUTHORIZATION, bearer(newAccess)))
                .andExpect(status().isOk());
    }

    @Test
    void forgotAndResetPassword() throws Exception {
        String email = uniqueEmail();
        registerUser(email, "Password1");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isOk());
        // Unknown e-mail gives the same answer (no user enumeration)
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "nobody@test.local"))))
                .andExpect(status().isOk());

        String mailBody = mailService.lastTo(email).orElseThrow().body();
        Matcher matcher = Pattern.compile("token=([A-Za-z0-9_-]+)").matcher(mailBody);
        assertThat(matcher.find()).isTrue();
        String resetToken = matcher.group(1);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", resetToken, "newPassword", "ResetPass3"))))
                .andExpect(status().isOk());
        // One-time token
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", resetToken, "newPassword", "ResetPass4"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RESET_TOKEN_INVALID"));

        assertThat(login(email, "ResetPass3").path("accessToken").asText()).isNotBlank();
    }

    @Test
    void userWithoutPermissionGetsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users").header(AUTHORIZATION, bearer(userToken())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(get("/api/v1/users").header(AUTHORIZATION, bearer(adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
