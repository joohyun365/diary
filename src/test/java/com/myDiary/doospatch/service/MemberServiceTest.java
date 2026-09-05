package com.myDiary.doospatch.service;

import com.myDiary.doospatch.dto.MemberRequestDto;
import com.myDiary.doospatch.dto.MemberResponseDto;
import com.myDiary.doospatch.entity.Member;
import com.myDiary.doospatch.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MemberServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Test
    public void createMemberTest(){
        MemberRequestDto memberRequestDto=new MemberRequestDto(
                "User1",
                "Username1",
                "testing@test.com",
                "12345678",
                "ROLE_USER");
        MemberResponseDto joined = memberService.join(memberRequestDto);
        UserDetails details = memberService.loadUserByUsername("Username1");
//        assertThat(details)
//                .extracting("name", "username", "email", "password", "auth")
//                .containsExactly(memberRequestDto.getName(), memberRequestDto.getUsername(), memberRequestDto.getEmail(), memberRequestDto.getPassword(), memberRequestDto.getAuth());
        assertAll(
            () -> assertThat(details.getUsername()).isEqualTo(memberRequestDto.getUsername()),
            () -> assertThat(passwordEncoder.matches(memberRequestDto.getPassword(), details.getPassword())).isTrue(),
            () -> assertThat(details.getAuthorities().iterator().next().getAuthority())
                    .isEqualTo(memberRequestDto.getAuth())
        );
    }
    @Nested
    @DisplayName("본인 유저네임 조회")
    class findUserByUsername{
        @Test
        @DisplayName("성공 - MemberResponseDto 반환")
        void success(){
            MemberRequestDto memberRequestDto = new MemberRequestDto("myName", "tester", "email@n.com", "12345678", "USER_ROLE");
            MemberResponseDto memberResponseDto = memberService.join(memberRequestDto);
            MemberResponseDto foundMemberResponseDto = memberService.findUserByUsername("tester");
            assertThat(foundMemberResponseDto.getUsername()).isEqualTo("tester");
        }
        @Test
        @DisplayName("실패 - MemberResponseDto 반환")
        void fail(){
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> memberService.findUserByUsername("fake"));
            assertThat(exception.getMessage())
                    .isEqualTo("해당 유저를 찾을 수 없습니다. User: fake");
        }
    }
    @Test
    public void deleteUserTest(){
        MemberRequestDto memberRequestDto = new MemberRequestDto("tester", "testdel", "testin@test.com", "135792468", "ROLE_USER");
        MemberResponseDto memberResponseDto = memberService.join(memberRequestDto);
        memberService.deleteMember(memberResponseDto.getUsername());
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> memberService.loadUserByUsername("testdel"));
        assertThat(exception.getMessage()).isEqualTo("가입되지 않은 유저입니다. Username: "+ "testdel");
    }
}
