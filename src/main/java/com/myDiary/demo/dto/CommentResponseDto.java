package com.myDiary.demo.dto;

import com.myDiary.demo.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponseDto {
    private Long id;
    private String username;
    private String content;

    public CommentResponseDto(Long id, String username, String content) {
        this.id = id;
        this.username = username;
        this.content = content;
    }
}
