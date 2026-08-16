package com.myDiary.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        String projectPath = System.getProperty("user.dir")+"/src/main/resources/static/images/";
        // 웹 브라우저에 /images/...로 요청이 들어오면 스프링이 가로채서 실제 폴더(projectPath)의 파일을 줌.
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///" + projectPath); // 파일 시스템에 직접 접근하는 접두사(file:///)
    }
}
