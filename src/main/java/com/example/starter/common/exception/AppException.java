package com.example.starter.common.exception;

import lombok.Getter;

/**
 * Throw this from services for any expected business error.
 * The message is translated by {@link GlobalExceptionHandler}; {@code args}
 * fill the {0}, {1}... placeholders of the message.
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] args;

    public AppException(ErrorCode errorCode, Object... args) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = args;
    }

    public static AppException notFound(ErrorCode code, Object id) {
        return new AppException(code, id);
    }
}
