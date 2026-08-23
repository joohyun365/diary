package com.myDiary.demo.controller;


import com.myDiary.demo.config.SecurityConfig;
import com.myDiary.demo.dto.MemberRequestDto;
import com.myDiary.demo.dto.MemberResponseDto;
import com.myDiary.demo.entity.Member;
import com.myDiary.demo.service.MemberService;
import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;


@WebMvcTest(MemberController.class) // 타겟 지정
@Import(SecurityConfig.class)  // 시큐리티 설정을 테스트 환경에 끌어옴 -> resolver가 @AuthenticationPrincipal를 해석할 수 있게 됨
public class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc; // 가짜 요청을 보내는 객체
    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON으로, 반대로도 변환해주는 잭슨 라이브러리
    @MockitoBean
    private MemberService memberService;

    @Test
    @WithMockUser // 인증된 가짜 유저를 시큐리티 컨텍스트에 주입
    @DisplayName("회원가입 API - 올바른 데이터를 보내면 201 Created와 생성된 유저 정보를 반환한다")
    void join_success() throws Exception{
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

    }

    @Test
//    @WithMockUser(username="testUser123") // 여기에 적힌 이름이 컨트롤러의 @AuthenticationPrincipal로 들어감-> 스프링 세큐리티 문제로 잠시 주석
    @DisplayName("회원 탈퇴 API - 로그인된 유저가 탈퇴를 요청하면 200 OK를 반환한다")
    void delete_success() throws Exception{
        mockMvc.perform(delete("/api/members")
                        .with(csrf())  // POST, PUT, DELETE 요청엔 csrf 필수
                        .with(user("testUser123").roles("USER"))) // 어노테이션 대신 유저 정보를 직접 강제로 넣음
                .andDo(print())
                .andExpect(status().isOk());
        verify(memberService).deleteMember("testUser123"); // deleteMember가 호출 됐는 지 확인
    }
}
