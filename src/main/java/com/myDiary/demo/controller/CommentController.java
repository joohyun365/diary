package com.myDiary.demo.controller;

import com.myDiary.demo.dto.CommentRequestDto;
import com.myDiary.demo.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/diaries/{diaryId}/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public String addComment(@PathVariable Long diaryId, @Valid @ModelAttribute CommentRequestDto commentRequestDto, BindingResult bindingResult, @AuthenticationPrincipal UserDetails userDetails){
        if (bindingResult.hasErrors()) {
            return "redirect:/diaries/" + diaryId + "?error=empty_comment";
        }
        commentService.joinComment(commentRequestDto, diaryId, userDetails.getUsername());
        return "redirect:/diaries/" + diaryId;
    }
    @PutMapping("/{commentId}")
    public String updateComment(@PathVariable Long diaryId, @PathVariable Long commentId, @Valid @ModelAttribute CommentRequestDto commentRequestDto, BindingResult bindingResult, @AuthenticationPrincipal UserDetails userDetails){
        if (bindingResult.hasErrors()) {
            return "redirect:/diaries/"+diaryId+"?error=empty_comment";
        }
        commentService.updateCommentById(commentId, commentRequestDto, userDetails.getUsername());
        return "redirect:/diaries/" + diaryId;
    }

    @DeleteMapping("/{commentId}")
    public String deleteComment(@PathVariable Long diaryId, @PathVariable Long commentId, @AuthenticationPrincipal UserDetails userDetails){
        commentService.deleteComment(commentId, userDetails.getUsername());
        return "redirect:/diaries/" + diaryId;
    }
}
