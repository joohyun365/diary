package com.myDiary.doospatch.controller;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.myDiary.doospatch.config.SecurityConfig;
import com.myDiary.doospatch.dto.DiaryRequestDto;
import com.myDiary.doospatch.dto.DiaryResponseDto;
import com.myDiary.doospatch.entity.Diary;
import com.myDiary.doospatch.service.DiaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiaryController.class)
@Import(SecurityConfig.class)  // 시큐리티 설정을 테스트 환경에 끌어옴 -> resolver가 @AuthenticationPrincipal를 해석할 수 있게 됨
public class DiaryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    DiaryService diaryService;

    @Test
    @DisplayName("다이어리 목록 API - 접속하면 다이어리 목록이 나오고 200 OK 반환")
    void diaryList_success() throws Exception{
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", // DiaryRequestDto의 필드명(imageFile)과 똑같아야 함
                "test.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );
        Diary diary= Diary.builder()
                .title("myTitle")
                .mood("HAPPY")
                .content("myDiaryContent")
                .imgPath("/images/random.jpg")
                .build();
        DiaryResponseDto expectedResponseDto = new DiaryResponseDto(diary);
        ReflectionTestUtils.setField(diary, "id", 1L);
        Diary diary2= Diary.builder()
                .title("myTitle2")
                .mood("SAD")
                .content("moreDiaryContent")
                .imgPath("/images/random2.jpg")
                .build();
        DiaryResponseDto expectedResponseDto2 = new DiaryResponseDto(diary2);
        ReflectionTestUtils.setField(diary2, "id", 2L);
        List<DiaryResponseDto> expectedList = new ArrayList<DiaryResponseDto>();
        expectedList.add(expectedResponseDto);
        expectedList.add(expectedResponseDto2);

        given(diaryService.findAll()).willReturn(expectedList);
        mockMvc.perform(get("/api/diaries")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value(expectedResponseDto.getTitle()))
                .andExpect(jsonPath("$[1].title").value(expectedResponseDto2.getTitle()))
        ;

    }

    @Test
    @DisplayName("다이어리 상세 API - 다이어리 들어가면 상세 내용 출력되고 200 OK 반환")
    void diaryDetail_success() throws Exception{
        Diary diary= Diary.builder()
                .title("myTitle")
                .mood("HAPPY")
                .content("myDiaryContent")
                .imgPath("/images/random.jpg")
                .build();
        DiaryResponseDto expectedResponseDto = new DiaryResponseDto(diary);
        ReflectionTestUtils.setField(diary, "id", 1L);

        given(diaryService.getDiaryById(any(Long.class))).willReturn(expectedResponseDto);
        mockMvc.perform(get("/api/diaries/1")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(expectedResponseDto.getTitle()))
                .andExpect(jsonPath("$.mood").value(expectedResponseDto.getMood()))
                .andExpect(jsonPath("$.content").value(expectedResponseDto.getContent()))
                .andExpect(jsonPath("$.imgPath").value(expectedResponseDto.getImgPath())
                );
    }
    @Nested
    @DisplayName("다이어리 추가 API")
    class addDiary {
        @Test
//    @WithMockUser(username = "testUser1")
        @DisplayName("성공 - 이미지와 텍스트를 폼 데이터로 보내면 201 CREATED 반환")
        void success() throws Exception {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "imageFile",
                    "test.jpg",
                    "image/jpeg",
                    "dummy image content".getBytes()
            );
            Diary diaryImg = Diary.builder()
                    .title("myTitle")
                    .content("myDiaryContent")
                    .mood("HAPPY")
                    .imgPath("/images/random.jpg")
                    .build();
            ReflectionTestUtils.setField(diaryImg, "id", 1L);
            DiaryResponseDto responseDto = new DiaryResponseDto(diaryImg);

            given(diaryService.joinDiary(any(DiaryRequestDto.class), any(String.class))).willReturn(responseDto);
            mockMvc.perform(multipart("/api/diaries") // post가 아닌 multipart로. param()으로 텍스트 넣기
                            .file(imageFile)
                            .param("title", "myTitle")
                            .param("content", "myDiaryContent")
                            .param("mood", "HAPPY")
                            .with(csrf())
                            .with(user("testUser1").roles("USER")))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value(responseDto.getTitle()))
                    .andExpect(jsonPath("$.mood").value(responseDto.getMood()))
                    .andExpect(jsonPath("$.content").value(responseDto.getContent()))
                    .andExpect(jsonPath("$.imgPath").value(responseDto.getImgPath()))
                    .andExpect(jsonPath("$.id").value(responseDto.getId()));
            verify(diaryService).joinDiary(any(DiaryRequestDto.class),anyString());
            // Matcher(any()같은 거)와 Raw Value를 섞어 쓰면 InvalidUseOfMatchersException가 뜸
        }

        @Test
        @DisplayName("실패 - 제목을 비워서 요청할 시 400 Bad Request 반환")
        void fail_no_title() throws Exception{
            // 제목이 비어있으면 컨트롤러 파라미터 앞에 붙은 @Valid가 service에 못 들어가게 해서 doThrow가 실행 안 됨
            mockMvc.perform(multipart("/api/diaries")
//                            .file(null) // 안 보내도 됨
                            .param("title", "")
                            .param("content","newContent")
                            .param("mood","HAPPY")
                            .with(user("testUser").roles("USER"))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().is4xxClientError());
            verify(diaryService, never()).joinDiary(any(), anyString());
        }
        @Test
        @DisplayName("실패 - 내용을 비어서 요청 시 400 Bad Request 반환")
        void fail_no_content() throws Exception{
            mockMvc.perform(multipart("/api/diaries")
                            .with(user("testUser").roles("USER"))
                            .param("title", "myTitle")
                            .param("content", "")
                            .param("mood", "HAPPY")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().is4xxClientError());
            verify(diaryService, never()).joinDiary(any(), anyString());
        }
    }

    @Test
//    @WithMockUser(username = "testUser12")
    @DisplayName("다이어리 수정 API - 올바른 양식으로 수정 시 200 OK 반환")
    void update_success() throws Exception{
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "test.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );
        Diary updatedDiary=Diary.builder()
                .title("updatedTitle")
                .content("myUpdatedContent")
                .mood("TIRED")
                .imgPath("/images/random2.jpg")
                .build();
        ReflectionTestUtils.setField(updatedDiary,"id",1L);
        DiaryResponseDto updatedResponseDto = new DiaryResponseDto(updatedDiary);

        given(diaryService.updateDiaryById(any(Long.class), any(DiaryRequestDto.class), any(String.class)))
                .willReturn(updatedResponseDto);
        mockMvc.perform(multipart("/api/diaries/1") // multipart는 기본이 post여서 형변환 필요.
                        .file(imageFile)
                        .param("title", "updatedTitle")
                        .param("content", "myUpdatedContent")
                        .param("mood", "TIRED")
                        .with(user("testUser12").roles("USER"))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(updatedResponseDto.getTitle()))
                .andExpect(jsonPath("$.mood").value(updatedResponseDto.getMood()))
                .andExpect(jsonPath("$.content").value(updatedResponseDto.getContent()))
                .andExpect(jsonPath("$.imgPath").value(updatedResponseDto.getImgPath()))
                .andExpect(jsonPath("$.id").value(updatedResponseDto.getId()));
        verify(diaryService).updateDiaryById(anyLong(), any(DiaryRequestDto.class), anyString());
    }

    @Test
    @DisplayName("다이어리 삭제 API - 다이어리 삭제 시 200 OK 반환")
    void deleteDiary_success() throws Exception{
        mockMvc.perform(delete("/api/diaries/1")
                .with(csrf())
                .with(user("testUser123").roles("USER")))
                .andDo(print())
                .andExpect(status().isOk());
                verify(diaryService).deleteDiaryById(1L, "testUser123");
    }

}
