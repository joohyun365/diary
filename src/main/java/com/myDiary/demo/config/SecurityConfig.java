package com.myDiary.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 잠시 postman 떄문에 비활
                // 폼 로그인 복구하되 REST 방식에 맞게 개조
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.getWriter().write("Login Success!");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(401);
                            response.getWriter().write("Login Failed!");
                        })
                        .permitAll()
                )

                // URL별 접근 권한 설정 (인가)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/diaries","/api/diaries/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members").permitAll()
                        .requestMatchers("/api/login","/login").permitAll()
                        // 그 외의 주소(글 작성, 수정, 삭제)는 무조건 로그인(인증)을 해야만 접근 가능
                        .anyRequest().authenticated()
                )
//                .httpBasic(basic -> basic.disable()) // 기본 HTTP 창도 비활성화
                // 권한 탈락 시 302 리다이렉트 대신 401(Unauthorized) 에러를 뱉도록 설정
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401); // 상태코드로 응답
//                            response.setContentType("application/json;charset=UTF-8");
//                            response.getWriter().write("{\"error\": \"로그인이 필요합니다.\"}");
                        })

                )


                // 로그아웃 설정
//                .logout(logout -> logout
//                        .logoutUrl("/api/members/logout") // 로그아웃 API 엔드포인트
//                        .logoutSuccessHandler((request, response, authentication) -> {
//                            response.setStatus(200);
//                        })
//                )
        ;

        return http.build();
    }

    // 💡 비밀번호 암호화 도구 (나중에 회원가입 서비스에서 가져다 씁니다)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}