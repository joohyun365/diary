package com.myDiary.demo.dto;

import com.myDiary.demo.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponseDto {
    private String content;

    public CommentResponseDto(Comment comment) {
        this.content = comment.getContent();
    }
}
