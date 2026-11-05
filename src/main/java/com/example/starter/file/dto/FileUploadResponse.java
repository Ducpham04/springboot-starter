package com.example.starter.file.dto;

/**
 * @param key         storage key, persist this in your entity (e.g. product.imageUrl)
 * @param url         URL to download the file through this API
 */
public record FileUploadResponse(String key, String originalName, String contentType, long size, String url) {
}
