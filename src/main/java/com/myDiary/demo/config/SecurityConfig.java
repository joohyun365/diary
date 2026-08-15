package com.myDiary.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                // 1. URL별 접근 권한 설정 (인가)
                .authorizeHttpRequests(auth -> auth
                        // css, js 같은 정적 파일과 목록 조회, 로그인, 회원가입은 아무나 접근 가능
                        .requestMatchers("/", "/diaries", "/join", "/login", "/css/**").permitAll()
                        // 그 외의 주소(글 작성, 수정, 삭제)는 무조건 로그인(인증)을 해야만 접근 가능
                        .anyRequest().authenticated()
                )

                // 2. 폼 로그인 설정 (세션 자동 사용)
                .formLogin(form -> form
                        // .loginPage("/login") // 💡 나중에 우리가 직접 예쁜 로그인 HTML을 만들면 이 주석을 풉니다!
                        .defaultSuccessUrl("/diaries", true) // 로그인 성공 시 기본으로 이동할 주소
                        .permitAll()
                )

                // 3. 로그아웃 설정
                .logout(logout -> logout
                        .logoutSuccessUrl("/diaries") // 로그아웃 성공 시 이동할 주소
                        .permitAll()
                );

        // 4. CSRF 방어 설정
        // Thymeleaf는 기본적으로 CSRF 토큰을 지원하므로 끄지 않고(disable 안 함) 두는 것이 보안 정석입니다.

        return http.build();
    }

    // 💡 비밀번호 암호화 도구 (나중에 회원가입 서비스에서 가져다 씁니다)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}