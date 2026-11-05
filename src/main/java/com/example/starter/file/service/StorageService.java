package com.example.starter.file.service;

import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * Storage abstraction. {@link LocalStorageService} is active when {@code app.storage.provider=local}.
 * To switch to S3/MinIO/GCS create e.g. {@code S3StorageService implements StorageService}
 * annotated with {@code @ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")}.
 */
public interface StorageService {

    /**
     * Stores the content and returns the generated key (e.g. {@code 2026/01/uuid.png}).
     */
    String store(InputStream content, String originalFilename, String contentType, long size);

    /** @throws com.example.starter.common.exception.AppException FILE_NOT_FOUND if missing */
    Resource load(String key);

    void delete(String key);
}
