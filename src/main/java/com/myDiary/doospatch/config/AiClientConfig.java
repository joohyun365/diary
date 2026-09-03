package com.myDiary.doospatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiClientConfig {
//    @Bean
//    public RestClient.Builder restClientBuilder() {
//        return RestClient.builder();
//    }

    @Bean
    public RestClient aiRestClient(@Value("${ai.server.url}") String aiServerUrl) {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(3000);// 연결 타임아웃 3초
        factory.setReadTimeout(5000); // 읽기 타임아웃 5초

        return RestClient.builder()
                .baseUrl(aiServerUrl)
                .requestFactory(factory)
                .build();
    }
}
