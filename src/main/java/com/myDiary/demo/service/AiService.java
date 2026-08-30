package com.myDiary.demo.service;

import com.myDiary.demo.dto.AiRequestDto;
import com.myDiary.demo.dto.AiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AiService {
    private final RestClient restClient;

    public AiResponseDto analyzeDiary(String content) {
        return restClient.post()
                .uri("/api/analyze") // 엔드포인트
                .body(new AiRequestDto(content))
                .retrieve()
                .body(AiResponseDto.class);
    }
}
