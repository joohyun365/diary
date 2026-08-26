package com.myDiary.demo.service;

import com.myDiary.demo.dto.AiRequestDto;
import com.myDiary.demo.dto.AiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {
    // 동기적인 통신을 지원하는 RestClient 객체 생성. 나중에 Spring HTTP Interfaces로 refactor할 예정
    private final RestClient restClient=RestClient.create();

    @Value("$ai.server.url")
    private String aiServerUrl;

    // 일기 내용을 FastAPI로 보내서 AI 분석 결과를 받아오는 메서드
    public AiResponseDto analyzeDiary(String content) {

        return restClient.post()
                .uri(aiServerUrl + "/api/analyze") // FastAPI의 엔드포인트
                .body(new AiRequestDto(content)) // DTO를 넣으면 알아서 JSON으로
                .retrieve() // 실행하고 응답을 가져옴
                .body(AiResponseDto.class); // 응답받은 JSON을 응답 DTO 형태로 매핑
    }
}
