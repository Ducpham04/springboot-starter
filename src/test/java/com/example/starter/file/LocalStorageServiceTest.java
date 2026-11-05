package com.example.starter.file;

import com.example.starter.common.exception.AppException;
import com.example.starter.config.AppProperties;
import com.example.starter.file.service.LocalStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService storage;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties(null, null,
                new AppProperties.Storage("local", tempDir.toString(), DataSize.ofMegabytes(1), List.of()),
                null, null, Locale.ENGLISH, null, false);
        storage = new LocalStorageService(props, Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void storesUnderDateFolderAndLoadsBack() throws Exception {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        String key = storage.store(new ByteArrayInputStream(data), "Report.PDF", "application/pdf", data.length);

        assertThat(key).startsWith("2026/03/").endsWith(".pdf");
        assertThat(storage.load(key).getContentAsByteArray()).isEqualTo(data);

        storage.delete(key);
        assertThatThrownBy(() -> storage.load(key)).isInstanceOf(AppException.class);
    }

    @Test
    void rejectsPathTraversal() {
        assertThatThrownBy(() -> storage.load("../secret.txt")).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> storage.load("2026/03/../../x")).isInstanceOf(AppException.class);
    }
}
