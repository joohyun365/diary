package com.myDiary.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AiExternalServiceException.class)
    public ResponseEntity<String> handleAiException(AiExternalServiceException e){
        if(e.getErrorType() == AiExternalServiceException.ErrorType.TIMEOUT){
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body("AI 서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요.");
        } else if(e.getErrorType() == AiExternalServiceException.ErrorType.CONNECTION_ERROR){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("AI 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.");
        } else if(e.getErrorType() == AiExternalServiceException.ErrorType.RESPONSE_ERROR){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("AI 서버 응답 형식이 올바르지 않습니다. 잠시 후 다시 시도해주세요.");
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("AI 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbidden(ForbiddenException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());
    }
}
