package com.myDiary.demo.service;

import com.myDiary.demo.dto.*;
import com.myDiary.demo.entity.Comment;
import com.myDiary.demo.entity.Diary;
import com.myDiary.demo.entity.Member;
import com.myDiary.demo.repository.DiaryRepository;
import com.myDiary.demo.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class CommentServiceTest {
    @Autowired
    private CommentService commentService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private DiaryService diaryService;
    @MockitoBean
    private AiService aiService; // 가짜 ai서비스

    private MultipartFile example;
    Diary diary;
    @BeforeEach
    public void setup(){
        Member member = Member.builder()
                .name("test")
                .username("tester")
                .email("testing@test.com")
                .password("12345678")
                .auth("ROLE_USER")
                .build();
        memberRepository.save(member);
        example = new MockMultipartFile(
                "imageFile",
                "test_image.jpg",
                "image/jpeg",
                "가짜 이미지 데이터입니다".getBytes()
        );
        when(aiService.analyzeDiary(anyString())) // 다이어리 서비스 호출 전에 가짜AI가 이 응답하도록
                .thenReturn(new AiResponseDto("HAPPY"));
        DiaryResponseDto diaryResponseDto = diaryService.joinDiary(new DiaryRequestDto("Today",
                        "testing comment",
                        "HAPPY",
                        example),
                "tester");
        diary=diaryService.findById(diaryResponseDto.getId());
    }
    @AfterEach
    public void deleteCreatedImg(){
        Member member = memberRepository.findByUsername("tester").orElse(null);
        if (member!=null) {
            diaryService.deleteAllImageFilesByMember(member);
        }
    }

    @Test
    public void joinCommentTest(){
        CommentResponseDto commentResponseDto = commentService.joinComment(new CommentRequestDto("Nice Comment"),
                diary.getId(),
                "tester");
        Comment comment=commentService.findById(commentResponseDto.getId());

        Member member = memberRepository.findByUsername("tester").orElse(null);
        assertThat(comment).
                extracting("content", "member","diary")
                .containsExactly("Nice Comment", member, diary);
    }
    @Test
    public void updateCommentByIdTest(){
        CommentRequestDto originalRequestDto = new CommentRequestDto("original comment");
        CommentResponseDto originalResponseDto = commentService.joinComment(originalRequestDto, diary.getId(), "tester");
        CommentRequestDto wantedCommentRequestDto = new CommentRequestDto("updated comment");
        Member member=memberRepository.findByUsername("tester").orElse(null);
        CommentResponseDto updatedResponseDto = commentService.updateCommentById(originalResponseDto.getId(), wantedCommentRequestDto, "tester");
        assertThat(updatedResponseDto).extracting(
                "id", "username","content"
        )
                .containsExactly(updatedResponseDto.getId(),updatedResponseDto.getUsername(), "updated comment");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()->commentService.updateCommentById(originalResponseDto.getId(), wantedCommentRequestDto, "notTester"));
        assertThat(e.getMessage()).isEqualTo("본인만 댓글을 수정할 수 있습니다.");
    }
}
