package com.myDiary.demo.service;

import com.myDiary.demo.config.AiClientConfig;
import com.myDiary.demo.dto.AiRequestDto;
import com.myDiary.demo.dto.AiResponseDto;
import com.myDiary.demo.exception.AiExternalServiceException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

// FastAPI 서버가 꺼져 있어도 테스트가 가능하도록 실제 HTTP 통신 대신 RestClient를 Mocking
class AiServiceTest {
    private AiService aiService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient.builder();
        // RestClient가 실제 FastAPI 대신 이 가짜 HTTP 서버를 보도록 설정
        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder
                .baseUrl("http://localhost:8000")
                .build();

       aiService = new AiService(restClient);
    }

    @Nested
    @DisplayName("감정 분석 API 테스트")
    class AnalyzeDiaryTests {
        @Test
        @DisplayName("성공 - FastAPI 서버 없이도 테스트 가능")
        void success() {
            mockServer.expect(requestTo("http://localhost:8000/api/analyze"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(
                            withSuccess(
                                   """
                                   {
                                    "analyzedMood": "JOY"
                                   }
                                   """,
                                   MediaType.APPLICATION_JSON
                            )
                   );

           AiResponseDto response = aiService.analyzeDiary("맛있는 음식을 먹었다.");

           assertThat(response).isNotNull();
           assertThat(response.analyzedMood()).isEqualTo("JOY");
           mockServer.verify();
        }

        @Test
        @DisplayName("실패 - 클라이언트에서 에러 발생 시 502 BAD_GATEWAY 발생") // 임시로 502
        void fail_clientError(){
            mockServer.expect(requestTo("http://localhost:8000/api/analyze"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(
                            withStatus(HttpStatus.BAD_REQUEST)
                    );
            AiExternalServiceException e = assertThrows(AiExternalServiceException.class,
                    ()->aiService.analyzeDiary("맛있는 음식을 먹었다."));
            assertThat(e.getErrorType()).isEqualTo(AiExternalServiceException.ErrorType.CLIENT_ERROR);
            mockServer.verify();
        }
        @Test
        @DisplayName("실패 - FastAPI 서버 내부 오류 발생 시 502 BAD_GATEWAY 발생")
        void fail_serverError(){
            mockServer.expect(requestTo("http://localhost:8000/api/analyze"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(
                            withServerError()
                    );
            AiExternalServiceException e = assertThrows(AiExternalServiceException.class,
                    () -> aiService.analyzeDiary("맛있는 음식을 먹었다."));
            assertThat(e.getErrorType()).isEqualTo(AiExternalServiceException.ErrorType.SERVER_ERROR);
            mockServer.verify();
        }
        @Test
        @DisplayName("실패 - FastAPI 서버 에서 옳지 않은 응답 반환 시 502 BAD_GATEWAY 발생")
        void fail_invalidResponse(){
            mockServer.expect(requestTo("http://localhost:8000/api/analyze"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(
                            withSuccess(
                                    """
                                    {
                                        "wrongField":
                                    }
                                    """,
                                    MediaType.APPLICATION_JSON
                            )
                    );
            AiExternalServiceException e = assertThrows(AiExternalServiceException.class,
                    () -> aiService.analyzeDiary("맛있는 음식을 먹었다."));
            assertThat(e.getErrorType()).isEqualTo(AiExternalServiceException.ErrorType.RESPONSE_ERROR);
            assertThat(e.getCause()).isInstanceOf(HttpMessageConversionException.class);
            mockServer.verify();
        }
        @Test
        @DisplayName("실패 - FastAPI 서버 연결 실패 시 CONNECTION_ERROR 발생")
        void fail_connectionFailure(){
            RestClient restClient = RestClient.builder()
                    .baseUrl("http://localhost:9999")
                    .build();
            aiService = new AiService(restClient);
            AiExternalServiceException e = assertThrows(AiExternalServiceException.class,
                    ()->aiService.analyzeDiary("맛있는 음식을 먹었다."));
            assertThat(e.getErrorType()).isEqualTo(AiExternalServiceException.ErrorType.CONNECTION_ERROR);
            assertThat(e.getCause()).isInstanceOf(RestClientException.class); // 원래 ResourceAccessException
        }
        // Timeout은 다음에
    }
}
