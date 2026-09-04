package com.myDiary.doospatch.controller;

import com.myDiary.doospatch.dto.CommentRequestDto;
import com.myDiary.doospatch.dto.CommentResponseDto;
import com.myDiary.doospatch.dto.DiaryResponseDto;
import com.myDiary.doospatch.repository.DiaryRepository;
import com.myDiary.doospatch.service.CommentService;
import com.myDiary.doospatch.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries/{diaryId}/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable("diaryId") Long diaryId){
        return ResponseEntity.ok()
                .body(commentService.findAllOnDiary(diaryId));
    }
    @PostMapping
    public ResponseEntity<CommentResponseDto> addComment(@PathVariable("diaryId") Long diaryId, @Valid @RequestBody CommentRequestDto commentRequestDto, @AuthenticationPrincipal UserDetails userDetails){

        CommentResponseDto commentResponseDto = commentService.joinComment(commentRequestDto, diaryId, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponseDto);
    }
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@PathVariable("diaryId") Long diaryId, @PathVariable Long commentId, @Valid @RequestBody CommentRequestDto commentRequestDto, @AuthenticationPrincipal UserDetails userDetails){

        CommentResponseDto commentResponseDto = commentService.updateCommentById(commentId, commentRequestDto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(commentResponseDto);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("diaryId") Long diaryId, @PathVariable Long commentId, @AuthenticationPrincipal UserDetails userDetails){
        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
