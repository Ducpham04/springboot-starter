package com.example.starter.file.service;

import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/** Stores files on the local disk under {@code app.storage.local-dir}/yyyy/MM/uuid.ext. */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private static final Pattern KEY_PATTERN = Pattern.compile("^\\d{4}/\\d{2}/[A-Za-z0-9-]+(\\.[A-Za-z0-9]{1,10})?$");

    private final Path root;
    private final Clock clock;

    public LocalStorageService(AppProperties props, Clock clock) {
        this.root = Path.of(props.storage().localDir()).toAbsolutePath().normalize();
        this.clock = clock;
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create storage directory " + root, e);
        }
    }

    @Override
    public String store(InputStream content, String originalFilename, String contentType, long size) {
        LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        String ext = extension(originalFilename);
        String key = "%d/%02d/%s%s".formatted(today.getYear(), today.getMonthValue(), UUID.randomUUID(), ext);
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
            return key;
        } catch (IOException e) {
            log.error("Failed to store file {}", key, e);
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    @Override
    public Resource load(String key) {
        Path file = resolve(key);
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new AppException(ErrorCode.FILE_NOT_FOUND);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            log.warn("Failed to delete file {}", key, e);
        }
    }

    /** Validates the key format and guards against path traversal. */
    private Path resolve(String key) {
        if (key == null || !KEY_PATTERN.matcher(key).matches()) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
        return path;
    }

    private static String extension(String filename) {
        String ext = StringUtils.getFilenameExtension(StringUtils.cleanPath(filename == null ? "" : filename));
        if (ext == null || !ext.matches("[A-Za-z0-9]{1,10}")) {
            return "";
        }
        return "." + ext.toLowerCase(Locale.ROOT);
    }
}
