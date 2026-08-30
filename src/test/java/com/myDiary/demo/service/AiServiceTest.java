package com.myDiary.demo.service;

import com.myDiary.demo.dto.AiRequestDto;
import com.myDiary.demo.dto.AiResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// FastAPI 서버가 꺼져 있어도 테스트가 가능하도록 실제 HTTP 통신 대신 RestClient를 Mocking
class AiServiceTest {
    private AiService aiService;

    @BeforeEach
    void setUp() {
       // 실제 RestClient를 가짜 객체로 만듦
       RestClient restClient = mock(RestClient.class);

       // RestClient.post() 호출 시 어떤 요청용 객체를 반환할지 지정
       RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class); // 엔드포인트
       RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class); // 요청 바디(나중에 JSON으로 바뀜)
       RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class); // 응답(자바 객체로 바뀐 응답 JSON)

       // 호출 체인: restClient.post() -> uri("/api/analyze") -> body(dto) -> retrieve() -> body(AiResponseDto.class)
       //    각 단계가 어떤 값을 반환할지 미리 설정
       when(restClient.post()).thenReturn(requestBodyUriSpec);
       when(requestBodyUriSpec.uri("/api/analyze")).thenReturn(requestBodySpec);
       when(requestBodySpec.body(any(AiRequestDto.class))).thenReturn(requestBodySpec);
       when(requestBodySpec.retrieve()).thenReturn(responseSpec);

       // 실제 서버가 아니라 테스트용으로 가짜 JSON 응답을 반환하도록 설정
       //    AiService.analyzeDiary()는 이 값을 AiResponseDto로 변환해서 돌려줌
       when(responseSpec.body(AiResponseDto.class))
               .thenReturn(new AiResponseDto("HAPPY"));

       // AiService 생성 시 mock RestClient를 주입한다.
       aiService = new AiService(restClient);
    }

    @Test
    @DisplayName("감정 분석 결과 받아오기 성공 - FastAPI 서버 없이도 테스트 가능")
    void analyzeDiaryTest() {
       String content = "행복하게 지낸 하루였다.";

       // 서비스 메서드를 호출하면, 미리 세팅한 mock 응답이 반환
       AiResponseDto response = aiService.analyzeDiary(content);

       assertThat(response).isNotNull();
       assertThat(response.analyzedMood()).isEqualTo("HAPPY");
    }
}
