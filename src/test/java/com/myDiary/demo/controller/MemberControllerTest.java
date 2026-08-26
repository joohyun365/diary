package com.myDiary.demo.controller;


import com.myDiary.demo.config.SecurityConfig;
import com.myDiary.demo.dto.MemberRequestDto;
import com.myDiary.demo.dto.MemberResponseDto;
import com.myDiary.demo.entity.Member;
import com.myDiary.demo.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(MemberController.class) // 타겟 지정
@Import(SecurityConfig.class)  // 시큐리티 설정을 테스트 환경에 끌어옴 -> resolver가 @AuthenticationPrincipal를 해석할 수 있게 됨
public class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc; // 가짜 요청을 보내는 객체
    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON으로, 반대로도 변환해주는 잭슨 라이브러리
    @MockitoBean
    private MemberService memberService;
@Nested
@DisplayName("회원 가입 API")
class joinMember{
    @Test
    @DisplayName("성공 - 올바른 데이터를 보내면 201 Created와 생성된 유저 정보를 반환한다")
    void join_success() throws Exception {
        MemberRequestDto requestDto = new MemberRequestDto(
                "name",
                "testUsername",
                "test@test.com",
                "12345678",
                "ROLE_USER");
        Member member = Member.builder()
                .name("name")
                .username("testUsername")
                .email("test@test.com")
                .password("12345678")
                .auth("ROLE_USER")
                .build();
        ReflectionTestUtils.setField(member, "id", 1L); // WebMVC는 가짜 환경이니 직접 id 넣어줌
        MemberResponseDto expectedResponse = new MemberResponseDto(member); // 예상 응답
        given(memberService.join(any(MemberRequestDto.class))).willReturn(expectedResponse);

        // 실제 요청 및 검증
        mockMvc.perform(post("/api/members")
                        .with(csrf()) // 시큐리티가 켜져 있으면 POST 요청 시 CSRF 토큰이 필요함
                        .contentType(MediaType.APPLICATION_JSON) // JSON 보낸다고 선언
                        .content(objectMapper.writeValueAsString(requestDto))) // 객체를 JSON 스트링으로 변환해서 바디에 담음
                .andDo(print()) // 콘솔에 요청/응답 전문을 이쁘게 출력해줌. 디버깅할 때 필수
                .andExpect(status().isCreated()) // 상태코드 확인
                .andExpect(jsonPath("$.username").value(expectedResponse.getUsername()))
                .andExpect(jsonPath("$.name").value(expectedResponse.getName())) // 추가 검증
                .andExpect(jsonPath("$.email").value(expectedResponse.getEmail())); // JSON 응답 객체의 특정 필드 검증
        verify(memberService).join(any(MemberRequestDto.class));
    }

    @Test
    @DisplayName("실패 - username 비워서 보내면 400 Bad Request 반환")
    void join_fail() throws Exception {
        MemberRequestDto requestDto = new MemberRequestDto(
                "name",
                "",
                "test@test.com",
                "12345678",
                "ROLE_USER");
        mockMvc.perform(post("/api/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(memberService, never()).join(requestDto);
    }
}

    @Nested // 보기 좋게 묶어서 봄
    @DisplayName("회원 탈퇴 API")
    class delete {
        @Test
//    @WithMockUser(username="testUser123") // 여기에 적힌 이름이 컨트롤러의 @AuthenticationPrincipal로 들어감-> 스프링 세큐리티 문제로 잠시 주석
        @DisplayName("성공 - 로그인된 유저가 탈퇴를 요청하면 200 OK를 반환한다")
        void success() throws Exception {
            mockMvc.perform(delete("/api/members")
                            .with(csrf())  // POST, PUT, DELETE 요청엔 csrf 필수
                            .with(user("testUser123").roles("USER"))) // 어노테이션 대신 유저 정보를 직접 강제로 넣음
                    .andDo(print())
                    .andExpect(status().isOk());
            verify(memberService).deleteMember("testUser123"); // deleteMember가 호출 됐는 지 확인
        }

        @Test
//    @WithMockUser(username = "ghostUser") // 인증된 가짜 유저를 시큐리티 컨텍스트에 주입
        @DisplayName("실패 - 없는 멤버 삭제하면 IllegalArgumentException 터짐 ")
        void fail_no_user() throws Exception {
            // given
            String ghostUsername = "ghostUser";
            String expectedErrorMessage = "no member to delete. Username: " + ghostUsername;
            doThrow(new IllegalArgumentException(expectedErrorMessage))
                    .when(memberService).deleteMember(ghostUsername);
            // when & then
            assertThatThrownBy(() ->
                    mockMvc.perform(delete("/api/members")
                            .with(csrf())
                            .with(user(ghostUsername).roles("USER")))
            )
                    .hasCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(expectedErrorMessage);
            verify(memberService).deleteMember(ghostUsername);
        }

        @Test
        @DisplayName("실패 - 인증 없이(로그인 안 하고) 접근하면 401 반환")
        void fail_no_session() throws Exception {
            // 유저 세팅을 안 함 (비로그인 사용자)
            mockMvc.perform(delete("/api/members")
                            .with(csrf())) // user 없이 보냄
                    .andDo(print())
                    .andExpect(status().isUnauthorized()); // is 4xx~ 보다 구체적으로
                    // SpringConfig 변경하고 바뀜
//                .andExpect(redirectedUrlPattern("**/login"))
                    // 스프링 내부의 AntPathMatcher가 깐깐해서 http://localhost 같은 프로토콜이 붙으면 패턴 해석을 못 함->
//                    .andExpect(redirectedUrl("/login")); // 완전 일치하게 바꿈
            verify(memberService,never()).deleteMember(any());
        }
    }
}
