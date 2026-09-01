package com.myDiary.demo.controller;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import com.myDiary.demo.config.SecurityConfig;
import com.myDiary.demo.dto.CommentRequestDto;
import com.myDiary.demo.dto.CommentResponseDto;
import com.myDiary.demo.entity.Comment;
import com.myDiary.demo.entity.Diary;
import com.myDiary.demo.entity.Member;
import com.myDiary.demo.service.CommentService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
public class CommentControllerTest {
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
