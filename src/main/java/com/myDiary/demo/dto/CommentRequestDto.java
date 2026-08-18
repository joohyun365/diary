package com.myDiary.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequestDto {
    private Long id;
    private String username;
    @NotBlank(message = "내용을 적으세요")
    private String content;

    public CommentRequestDto(Long id, String username, String content) {
        this.id = id;
        this.username = username;
        this.content = content;
    }
}
