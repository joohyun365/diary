package com.myDiary.doospatch.dto;

import com.myDiary.doospatch.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponseDto {
    private Long id;
    private String username;
    private String content;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.username = comment.getMember().getUsername();
        this.content = comment.getContent();
    }
}
