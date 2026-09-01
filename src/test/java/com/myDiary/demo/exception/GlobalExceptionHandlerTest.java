package com.myDiary.demo.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp(){
        handler= new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("AI 서버 응답 시간 초과 -> 504 Gateway Timeout")
    void timeout(){

        AiExternalServiceException exception=
                new AiExternalServiceException(
                        "AI 서버 응답 시간 초과",
                    new ResourceAccessException("Timeout"),
                    AiExternalServiceException.ErrorType.TIMEOUT
                );

        ResponseEntity<String> response = handler.handleAiException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody()).
                isEqualTo("AI 서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요.");
    }

    @Test
    @DisplayName("AI 서버 연결 안 됨 -> 502 CONNECTION_ERROR")
    void connectionError(){
        AiExternalServiceException exception = new AiExternalServiceException(
                "AI 서버에 연결할 수 없습니다",
                new RestClientException("Connection failed"),
                AiExternalServiceException.ErrorType.CONNECTION_ERROR
        );
        ResponseEntity<String> response = handler.handleAiException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isEqualTo("AI 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    @Test
    @DisplayName("AI 서버 응답 오류 -> RESPONSE_ERROR")
    void responseError(){
        AiExternalServiceException exception = new AiExternalServiceException(
                "AI 서버 응답 오류",
                new HttpMessageConversionException("Wrong response format"),
                AiExternalServiceException.ErrorType.RESPONSE_ERROR
        );
        ResponseEntity<String> response = handler.handleAiException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isEqualTo("AI 서버 응답 형식이 올바르지 않습니다. 잠시 후 다시 시도해주세요.");
    }
    @Test
    @DisplayName("AI 서버 4xx 클라이언트 오류 -> 502 BAD_GATEWAY")
    void clientError(){
        AiExternalServiceException exception = new AiExternalServiceException(
                "AI 서버 클라이언트 오류",
                null,
                AiExternalServiceException.ErrorType.CLIENT_ERROR
        );
        ResponseEntity<String> response = handler.handleAiException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isEqualTo("AI 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
    @Test
    @DisplayName("AI 서버 5xx 내부 오류 -> 502 BAD_GATEWAY")
    void serverError(){
        AiExternalServiceException exception = new AiExternalServiceException(
                "AI 서버 내부 오류",
                null,
                AiExternalServiceException.ErrorType.SERVER_ERROR
        );
        ResponseEntity<String> response = handler.handleAiException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isEqualTo("AI 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

}
