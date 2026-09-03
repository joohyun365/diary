package com.myDiary.doospatch.service;

import com.myDiary.doospatch.dto.MemberRequestDto;
import com.myDiary.doospatch.dto.MemberResponseDto;
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
    @Test
    public void deleteUserTest(){
        MemberRequestDto memberRequestDto = new MemberRequestDto("tester", "testdel", "testin@test.com", "135792468", "ROLE_USER");
        MemberResponseDto memberResponseDto = memberService.join(memberRequestDto);
        memberService.deleteMember(memberResponseDto.getUsername());
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> memberService.loadUserByUsername("testdel"));
        assertThat(exception.getMessage()).isEqualTo("가입되지 않은 유저입니다. Username: "+ "testdel");
    }
}
