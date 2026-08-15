package com.myDiary.demo.dto;

import com.myDiary.demo.entity.Member;
import lombok.Getter;

@Getter
public class MemberResponseDto {
    private long id;
    private String name;
    private String username;
    private String email;
    private String auth;

    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.username = member.getUsername();
        this.email = member.getEmail();
        this.auth = member.getAuth();
    }
}
