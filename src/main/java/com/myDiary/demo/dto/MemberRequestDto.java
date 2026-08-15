package com.myDiary.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberRequestDto {
    @NotBlank(message = "이름은 필수")
    private String name;
    @NotBlank(message = "유저네임은 필수")
    private String username;
    @Email
    private String email;
    @NotBlank(message = "비밀번호는 필수")
    @Size(min = 8, message = "8자리 이상")
    private String password;
    private String auth;

    public MemberRequestDto(String name, String username, String email, String password, String auth) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.auth = auth;
    }
}
