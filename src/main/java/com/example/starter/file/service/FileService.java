package com.example.starter.file.service;

import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.config.AppProperties;
import com.example.starter.file.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** Validation (size, content type) on top of the pluggable {@link StorageService}. */
@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageService storageService;
    private final AppProperties props;

    public FileUploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > props.storage().maxFileSize().toBytes()) {
            throw new AppException(ErrorCode.PAYLOAD_TOO_LARGE);
        }
        List<String> allowed = props.storage().allowedContentTypes();
        String contentType = file.getContentType();
        if (allowed != null && !allowed.isEmpty() && (contentType == null || !allowed.contains(contentType))) {
            throw new AppException(ErrorCode.FILE_TYPE_NOT_ALLOWED, contentType);
        }
        try (InputStream in = file.getInputStream()) {
            String key = storageService.store(in, file.getOriginalFilename(), contentType, file.getSize());
            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/files").queryParam("key", key).toUriString();
            return new FileUploadResponse(key, file.getOriginalFilename(), contentType, file.getSize(), url);
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }
}
