package com.myDiary.doospatch.controller;

import com.myDiary.doospatch.dto.CommentRequestDto;
import com.myDiary.doospatch.dto.CommentResponseDto;
import com.myDiary.doospatch.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries/{diaryId}/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDto> addComment(@PathVariable Long diaryId, @Valid @RequestBody CommentRequestDto commentRequestDto, @AuthenticationPrincipal UserDetails userDetails){

        CommentResponseDto commentResponseDto = commentService.joinComment(commentRequestDto, diaryId, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponseDto);
    }
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@PathVariable Long diaryId, @PathVariable Long commentId, @Valid @RequestBody CommentRequestDto commentRequestDto, @AuthenticationPrincipal UserDetails userDetails){

        CommentResponseDto commentResponseDto = commentService.updateCommentById(commentId, commentRequestDto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(commentResponseDto);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long diaryId, @PathVariable Long commentId, @AuthenticationPrincipal UserDetails userDetails){
        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
