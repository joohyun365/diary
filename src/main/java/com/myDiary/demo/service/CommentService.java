package com.myDiary.demo.service;

import com.myDiary.demo.dto.CommentRequestDto;
import com.myDiary.demo.dto.CommentResponseDto;
import com.myDiary.demo.entity.Comment;
import com.myDiary.demo.entity.Diary;
import com.myDiary.demo.entity.Member;
import com.myDiary.demo.repository.CommentRepository;
import com.myDiary.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final DiaryService diaryService;

    @Transactional
    public CommentResponseDto joinComment(CommentRequestDto commentRequestDto, Long diaryId, String currentUsername) {
        Member member = memberRepository.findByUsername(currentUsername).orElseThrow(
                () -> new UsernameNotFoundException("존재하지 않는 유저는 댓글을 적을 수 없습니다.")
        );
        Diary diary =diaryService.findById(diaryId);
        Comment comment = Comment.builder()
                .content(commentRequestDto.getContent())
                .member(member)
                .diary(diary)
                .build();
        commentRepository.save(comment);
        member.addComment(comment);
        diary.addComment(comment);
        return new CommentResponseDto(comment.getId(), member.getUsername(),comment.getContent());
    }

    @Transactional
    public CommentResponseDto updateCommentById(Long commentId, CommentRequestDto commentRequestDto, String currentUsername){
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 댓글입니다.")
        );
        if(!comment.getMember().getUsername().equals(currentUsername)){
            throw new IllegalArgumentException("본인만 댓글을 수정할 수 있습니다.");
        }
        comment.updateComment(commentRequestDto.getContent());
        return new CommentResponseDto(comment.getId(), currentUsername, comment.getContent());
    }

    @Transactional
    public void deleteComment(Long commentId, String currentUsername){
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 댓글입니다.")
        );
        if(!comment.getMember().getUsername().equals(currentUsername)){
            throw new IllegalArgumentException("본인만 댓글을 삭제할 수 있습니다.");
        }
        commentRepository.deleteById(commentId);

    }

    public List<CommentResponseDto> findAllOnDiary(Long diaryId){
        List<Comment> commentList = commentRepository.findAllByDiaryId(diaryId);
        return commentList.stream()
                .map(comment->new CommentResponseDto(comment.getId(),
                        comment.getMember().getUsername(),
                        comment.getContent()))
                .toList();
    }
    public Comment findById(Long id){
        Comment comment= commentRepository.findById(id).orElseThrow(
                ()->new IllegalArgumentException("찾고 있는 댓글이 없습니다.")
        );
        return comment;
    }
}
