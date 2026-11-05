package com.example.starter.file;

import com.example.starter.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileIT extends AbstractIntegrationTest {

    @Test
    void uploadThenDownload() throws Exception {
        String token = userToken();
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain",
                "xin chào".getBytes(StandardCharsets.UTF_8));

        String key = body(mockMvc.perform(multipart("/api/v1/files").file(file)
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.key").exists())
                .andExpect(jsonPath("$.data.size").value(file.getSize()))
                .andReturn()).path("data").path("key").asText();

        mockMvc.perform(get("/api/v1/files").param("key", key).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(file.getBytes()));
    }

    @Test
    void rejectsDisallowedContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "evil.sh", "application/x-sh", "rm -rf".getBytes());
        mockMvc.perform(multipart("/api/v1/files").file(file).header(AUTHORIZATION, bearer(userToken())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FILE_TYPE_NOT_ALLOWED"));
    }

    @Test
    void pathTraversalKeyIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/files").param("key", "../../etc/passwd")
                        .header(AUTHORIZATION, bearer(userToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FILE_NOT_FOUND"));
    }
}
