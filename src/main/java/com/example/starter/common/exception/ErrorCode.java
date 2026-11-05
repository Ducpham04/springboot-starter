package com.example.starter.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Catalogue of business error codes. {@code messageKey} is resolved from
 * {@code i18n/messages*.properties} using the request locale (Accept-Language).
 * Add new codes here when you add a module.
 */
@Getter
public enum ErrorCode {
    // Generic
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "error.validation"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "error.bad-request"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.not-found"),
    CONFLICT(HttpStatus.CONFLICT, "error.conflict"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "error.method-not-allowed"),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "error.payload-too-large"),

    // Security
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "error.unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "error.forbidden"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "error.auth.invalid-credentials"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "error.auth.account-disabled"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "error.auth.token-invalid"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "error.auth.token-expired"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "error.auth.refresh-token-invalid"),
    RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "error.auth.reset-token-invalid"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "error.auth.email-exists"),
    OLD_PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "error.auth.old-password-incorrect"),

    // File
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "error.file.empty"),
    FILE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "error.file.type-not-allowed"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.file.not-found"),
    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.file.storage"),

    // Product (module example)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "error.product.not-found"),
    PRODUCT_SKU_EXISTS(HttpStatus.CONFLICT, "error.product.sku-exists");

    private final HttpStatus status;
    private final String messageKey;

    ErrorCode(HttpStatus status, String messageKey) {
        this.status = status;
        this.messageKey = messageKey;
    }
}
