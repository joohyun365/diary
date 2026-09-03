package com.myDiary.doospatch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequestDto {
    @NotBlank(message = "내용을 적으세요")
    private String content;

    public CommentRequestDto(String content) {
        this.content = content;
    }
}
