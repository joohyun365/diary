package com.myDiary.doospatch.controller;

import com.myDiary.doospatch.dto.MemberRequestDto;
import com.myDiary.doospatch.dto.MemberResponseDto;
import com.myDiary.doospatch.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponseDto> create(@Valid @RequestBody MemberRequestDto memberRequestDto) {

        MemberResponseDto memberResponseDto = memberService.join(memberRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberResponseDto);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        memberService.deleteMember(userDetails.getUsername());
        // DB에서 지웠으니 현재 브라우저의 로그인 상태(세션)도 완전히 폭파시킴.
        SecurityContextHolder.clearContext();
        // 세션이 존재할 때만 지우도록 안전하게 처리
        var session = request.getSession(false);
        if(session!=null) {
            request.getSession().invalidate();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
