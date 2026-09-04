package com.myDiary.doospatch.controller;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import com.myDiary.doospatch.config.SecurityConfig;
import com.myDiary.doospatch.dto.CommentRequestDto;
import com.myDiary.doospatch.dto.CommentResponseDto;
import com.myDiary.doospatch.entity.Comment;
import com.myDiary.doospatch.entity.Diary;
import com.myDiary.doospatch.entity.Member;
import com.myDiary.doospatch.exception.GlobalExceptionHandler;
import com.myDiary.doospatch.exception.ResourceNotFoundException;
import com.myDiary.doospatch.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CommentController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
public class CommentControllerTest {
    @Autowired
    @MockitoBean
    CommentService commentService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    private Member member;
    private Diary diary;

    @BeforeEach
    void setUp(){
        member = new Member("John",
                "tester",
                "tes@t.com",
                "12345678",
                "USER");
        diary = Diary.builder()
                .title("myDiary")
                .content("myContents")
                .mood("JOY")
                .member(member)
                .imgPath("path")
                .build();
        ReflectionTestUtils.setField(member,"id",1L);
        ReflectionTestUtils.setField(diary,"id",1L);
    }
    @Nested
    @DisplayName("댓글 조회 API")
    class getCommentsTest{
        @Test
        @DisplayName("성공 - 다이어리의 모든 댓글 조회 시 200 OK 반환")
        void success()throws Exception{
            Comment firstComment = new Comment("first comment", member, diary);
            Comment secondComment = new Comment("second comment", member, diary);
            ReflectionTestUtils.setField(firstComment,"id",1L);
            ReflectionTestUtils.setField(secondComment,"id",2L);
            CommentResponseDto firstCommentResponseDto = new CommentResponseDto(firstComment);
            CommentResponseDto secondCommentResponseDto = new CommentResponseDto(secondComment);
            List<CommentResponseDto> expectedList = new ArrayList<>();
            expectedList.add(firstCommentResponseDto);
            expectedList.add(secondCommentResponseDto);

            given(commentService.findAllOnDiary(eq(1L))).willReturn(expectedList);
            mockMvc.perform(get("/api/diaries/1/comments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].content").value("first comment"))
                    .andExpect(jsonPath("$[1].content").value("second comment"))
            ;
        }
        @Test
        @DisplayName("실패 - 없는 다이어리의 댓글 조회하면 404 NOT_FOUND 반환")
        void fail_noDiary() throws Exception {
            ResourceNotFoundException expectedException =
                    new ResourceNotFoundException("해당 일기가 존재하지 않습니다. id: -1");
            given(commentService.findAllOnDiary(eq(-1L))).willThrow(expectedException);
            mockMvc.perform(get("/api/diaries/-1/comments"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(commentService).findAllOnDiary(eq(-1L));
        }
    }

    @Nested
    @DisplayName("댓글 작성 API")
    class addCommentTest {
        @Test
        @DisplayName("성공 - 올바른 형식으로 댓글 작성 시 201 CREATED 반환")
        void success() throws Exception {
            Comment comment = Comment.builder()
                    .member(member)
                    .content("new comment")
                    .diary(diary)
                    .build();

            ReflectionTestUtils.setField(comment, "id", 3L);
            CommentRequestDto commentRequestDto = new CommentRequestDto("new comment");
            CommentResponseDto expectedResponse = new CommentResponseDto(comment);
            given(commentService.joinComment(any(CommentRequestDto.class), eq(1L), eq("tester")))
                    .willReturn(expectedResponse);
            mockMvc.perform(post("/api/diaries/1/comments")
                            .with(csrf())
                            .with(user("tester").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentRequestDto)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
                    .andExpect(jsonPath("$.username").value(expectedResponse.getUsername()))
                    .andExpect(jsonPath("$.content").value(expectedResponse.getContent()));
            verify(commentService).joinComment(any(CommentRequestDto.class), eq(1L), eq("tester"));
        }
        @Test
        @DisplayName("실패 - 내용 없이 댓글 작성하면 400 BAD_REQUEST 반환")
        void fail_noContent() throws Exception {
            CommentRequestDto commentRequestDto = new CommentRequestDto("");
            mockMvc.perform(post("/api/diaries/1/comments")
                            .with(csrf())
                            .with(user("tester").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentRequestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class updateCommentTest {
        @Test
        @DisplayName("성공 - 올바른 형식으로 댓글 수정 시 200 OK 반환")
        void success() throws Exception {
            Comment updatedComment = Comment.builder()
                    .member(member)
                    .content("updated comment")
                    .diary(diary)
                    .build();
            CommentRequestDto commentRequestDto = new CommentRequestDto("updated comment");
            CommentResponseDto expectedResponseDto = new CommentResponseDto(updatedComment);
            ReflectionTestUtils.setField(updatedComment, "id", 4L);
            given(commentService.updateCommentById(eq(4L),
                    any(CommentRequestDto.class),
                    eq("tester")))
                    .willReturn(expectedResponseDto);
            mockMvc.perform(put("/api/diaries/1/comments/4")
                            .with(csrf())
                            .with(user("tester").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentRequestDto)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.id").value(expectedResponseDto.getId()))
                    .andExpect(jsonPath("$.username").value(expectedResponseDto.getUsername()))
                    .andExpect(jsonPath("$.content").value(expectedResponseDto.getContent()));
            verify(commentService).updateCommentById(eq(4L),any(CommentRequestDto.class),eq("tester"));
        }
        @Test
        @DisplayName("실패 - 내용 비우고 댓글 수정 요청하면 400 BAD_REQUEST 반환")
        void fail_noContent() throws Exception{
            CommentRequestDto commentRequestDto = new CommentRequestDto("");
            mockMvc.perform(put("/api/diaries/1/comments/5")
                    .with(csrf())
                    .with(user("notTester").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentRequestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void deleteComment() throws Exception {
        mockMvc.perform(delete("/api/diaries/1/comments/5")
                        .with(csrf())
                        .with(user("tester").roles("USER")))
                .andDo(print())
                .andExpect(status().isOk());
        verify(commentService).deleteComment(5L,"tester");
    }
}
