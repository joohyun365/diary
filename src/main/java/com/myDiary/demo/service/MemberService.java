package com.myDiary.demo.service;

import com.myDiary.demo.dto.MemberRequestDto;
import com.myDiary.demo.dto.MemberResponseDto;
import com.myDiary.demo.entity.Member;
import com.myDiary.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final DiaryService diaryService;
    private final PasswordEncoder passwordEncoder;

    public MemberResponseDto join(MemberRequestDto memberRequestDto) {
        String encodedPassword = passwordEncoder.encode(memberRequestDto.getPassword());
        Member newMember = Member.builder()
                .name(memberRequestDto.getName())
                .username(memberRequestDto.getUsername())
                .email(memberRequestDto.getEmail())
                .password(encodedPassword)
                .auth("ROLE_USER")
                .build();
        memberRepository.save(newMember);
        return new MemberResponseDto(newMember);
    }

    public void deleteMember(String username){
        Member member = memberRepository.findByUsername(username).orElseThrow(
                ()->new UsernameNotFoundException("no member to delete. Username: " + username)
        );
        diaryService.deleteAllImageFilesByMember(member);
        memberRepository.deleteByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("가입되지 않은 유저입니다. Username: " + username)
        );
        return User.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .authorities(member.getAuth())
                .build();
    }
}