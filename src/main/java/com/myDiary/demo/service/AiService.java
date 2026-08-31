package com.myDiary.demo.service;

import com.myDiary.demo.dto.AiRequestDto;
import com.myDiary.demo.dto.AiResponseDto;
import com.myDiary.demo.exception.AiExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class AiService {
    private final RestClient restClient;

    public AiResponseDto analyzeDiary(String content) {

        try {
            return restClient.post()
                    .uri("/api/analyze") // 엔드포인트
                    .body(new AiRequestDto(content))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new RestClientException("서버 요청 오류: " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new RestClientException("서버 장애: " + res.getStatusCode());
                    })
                    .body(AiResponseDto.class);
        } catch (ResourceAccessException e) {
            throw new AiExternalServiceException("AI 서버 응답 시간 초과", e, AiExternalServiceException.ErrorType.CONNECTION_ERROR); // 일단 AiExternalServiceException.ErrorType.TIMEOUT 대신
        } catch (HttpMessageConversionException e) { // JSON 변환 오류 또는 DTO 매핑 실패
            throw new AiExternalServiceException("AI 서버 응답 형식이 올바르지 않습니다.", e, AiExternalServiceException.ErrorType.RESPONSE_ERROR);
        } catch (RestClientException e) {
            throw new AiExternalServiceException("AI 서버 연결 실패", e, AiExternalServiceException.ErrorType.CONNECTION_ERROR);
        }
    }

}
