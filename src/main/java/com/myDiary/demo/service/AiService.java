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
                        throw new AiExternalServiceException("서버 클라이언트 오류: " + res.getStatusCode(),
                                null,
                                AiExternalServiceException.ErrorType.CLIENT_ERROR);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new AiExternalServiceException("서버 내부 장애: " + res.getStatusCode(),
                                null,
                                AiExternalServiceException.ErrorType.SERVER_ERROR);
                    })
                    .body(AiResponseDto.class);
        } catch (AiExternalServiceException e) {
            throw e;
        }catch (ResourceAccessException e) {
            throw new AiExternalServiceException("" +
                    "AI 서버 연결에 실패했습니다.",
                    e,
                    AiExternalServiceException.ErrorType.CONNECTION_ERROR);
        } catch (HttpMessageConversionException e) { // JSON 변환 오류 또는 DTO 매핑 실패
            throw new AiExternalServiceException(

                    "AI 서버 응답 형식이 올바르지 않습니다.",
                    e,
                    AiExternalServiceException.ErrorType.RESPONSE_ERROR);
        } catch (RestClientException e) {
            throw new AiExternalServiceException(
                    "AI 서버 통신 중 오류가 발생했습니다.",
                    e,
                    AiExternalServiceException.ErrorType.CONNECTION_ERROR);
        }
    }

}
