package com.myDiary.demo.controller;

import com.myDiary.demo.dto.MemberRequestDto;
import com.myDiary.demo.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/join")
public class MemberController {
    private final MemberService memberService;

    @GetMapping
    public String joinForm(){
        return "member/join";
    }
    @PostMapping
    public String create(@Valid @ModelAttribute MemberRequestDto memberRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "member/join";
        }
        memberService.join(memberRequestDto);
        return "redirect:/diaries";
    }

//    @DeleteMapping("/withdraw")
    @GetMapping("/withdraw")
    public String deleteUser(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        memberService.deleteMember(userDetails.getUsername());
        // DB에서 지웠으니 현재 브라우저의 로그인 상태(세션)도 완전히 폭파시킴.
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
        return "redirect:/join";
    }
}
