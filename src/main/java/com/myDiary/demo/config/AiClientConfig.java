package com.myDiary.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiClientConfig {
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient aiRestClient(RestClient.Builder builder,
                                  @Value("${ai.server.url}") String aiServerUrl) {
        return builder.baseUrl(aiServerUrl).build();
    }
}
