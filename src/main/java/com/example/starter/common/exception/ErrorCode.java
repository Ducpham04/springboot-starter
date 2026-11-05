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
    REGISTRATION_DISABLED(HttpStatus.FORBIDDEN, "error.auth.registration-disabled"),

    // Users
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "error.user.not-found"),
    ROLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "error.user.role-not-found"),
    CANNOT_MODIFY_SELF(HttpStatus.BAD_REQUEST, "error.user.cannot-modify-self"),

    // File
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "error.file.empty"),
    FILE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "error.file.type-not-allowed"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.file.not-found"),
    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.file.storage"),

    // Product (module example)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "error.product.not-found"),
    PRODUCT_SKU_EXISTS(HttpStatus.CONFLICT, "error.product.sku-exists"),
    PRODUCT_IN_USE(HttpStatus.CONFLICT, "error.product.in-use"),
    PRODUCT_INACTIVE(HttpStatus.BAD_REQUEST, "error.product.inactive"),

    // Category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "error.category.not-found"),
    CATEGORY_NAME_EXISTS(HttpStatus.CONFLICT, "error.category.name-exists"),
    CATEGORY_IN_USE(HttpStatus.CONFLICT, "error.category.in-use"),

    // Customer
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "error.customer.not-found"),
    CUSTOMER_PHONE_EXISTS(HttpStatus.CONFLICT, "error.customer.phone-exists"),
    CUSTOMER_IN_USE(HttpStatus.CONFLICT, "error.customer.in-use"),

    // Orders
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "error.order.not-found"),
    ORDER_ALREADY_CANCELLED(HttpStatus.CONFLICT, "error.order.already-cancelled"),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "error.order.insufficient-stock"),
    DUPLICATE_LINE_ITEM(HttpStatus.BAD_REQUEST, "error.order.duplicate-item"),
    DISCOUNT_TOO_LARGE(HttpStatus.BAD_REQUEST, "error.order.discount-too-large"),
    CASH_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "error.order.cash-not-enough"),

    // Inventory
    RECEIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "error.inventory.receipt-not-found"),
    STOCK_ADJUSTMENT_NO_CHANGE(HttpStatus.BAD_REQUEST, "error.inventory.no-change");

    private final HttpStatus status;
    private final String messageKey;

    ErrorCode(HttpStatus status, String messageKey) {
        this.status = status;
        this.messageKey = messageKey;
    }
}
