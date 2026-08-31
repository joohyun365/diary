package com.myDiary.demo.exception;

public class AiExternalServiceException extends RuntimeException {
    public enum ErrorType {
        TIMEOUT,
//        CLIENT_ERROR,
//        SERVER_ERROR,
        RESPONSE_ERROR,
        CONNECTION_ERROR
    }
    private final ErrorType errorType;

    public AiExternalServiceException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
