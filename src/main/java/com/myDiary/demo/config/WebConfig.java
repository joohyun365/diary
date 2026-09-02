package com.myDiary.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.dir}")
    private String fileDir;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        String resourceLocation = Path.of(fileDir) // OS가 URI 변환 신경 덜 쓰게 함
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
        // 웹 브라우저에 /images/...로 요청이 들어오면 스프링이 가로채서 실제 폴더(projectPath)의 파일을 줌.
        registry.addResourceHandler("/images/**")
                .addResourceLocations(resourceLocation);
    }
}
