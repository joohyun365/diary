package com.myDiary.demo.service;

import com.myDiary.demo.dto.DiaryRequestDto;
import com.myDiary.demo.dto.DiaryResponseDto;
import com.myDiary.demo.entity.Diary;
import com.myDiary.demo.entity.Member;
import com.myDiary.demo.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class DiaryServiceTest {
    @Autowired // Spring Boot Test에서는 스프링이 아닌 JUNIT5가 관리하기 떄문에 @RequiredArgsConstructor 작동 안 함
    private DiaryService diaryService;
    @Autowired
    private MemberRepository memberRepository;

    private MultipartFile example;
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
    }
    @AfterEach
    public void deleteCreatedImg(){
        Member member = memberRepository.findByUsername("tester").orElse(null);
        if (member!=null) {
            diaryService.deleteAllImageFilesByMember(member);
        }
    }

    @Test
    public void joinDiaryTest(){
        DiaryRequestDto diaryRequestDto = new DiaryRequestDto("new title", "some content", "HAPPY", example);
        DiaryResponseDto diaryResponseDto = diaryService.joinDiary(diaryRequestDto, "tester");
        Diary diary = diaryService.findById(diaryResponseDto.getId());
        assertThat(diary.getTitle()).isEqualTo("new title");
        assertThat(diary.getContent()).isEqualTo("some content");
        assertThat(diary.getMood()).isEqualTo("HAPPY");
        assertThat(diary.getMember().getUsername()).isEqualTo("tester");
        assertThat(diary.getImgPath()).isEqualTo(diaryResponseDto.getImgPath());
        /*
        * assertThat(diary)
        .extracting("title", "content", "mood")
        .containsExactly("new title", "some content", "happy");
        * */
    }

    @Test
    public void readDiaryTest(){
        DiaryRequestDto diaryRequestDto = new DiaryRequestDto("Title1", "Today I..", "TIRED", example);
        DiaryResponseDto diaryResponseDto = diaryService.joinDiary(diaryRequestDto, "tester");
        Diary diary = diaryService.findById(diaryResponseDto.getId());
        assertAll("일기 읽기 검증",
                ()->assertThat(diary.getId()).isEqualTo(diaryResponseDto.getId()),
                ()->assertThat(diary.getTitle()).isEqualTo(diaryResponseDto.getTitle()),
                ()->assertThat(diary.getContent()).isEqualTo(diaryResponseDto.getContent()),
                ()->assertThat(diary.getMood()).isEqualTo(diaryResponseDto.getMood()),
                ()->assertThat(diary.getMember().getUsername()).isEqualTo("tester"),
                ()->assertThat(diary.getImgPath()).isEqualTo(diaryResponseDto.getImgPath())
        );
    }
    @Test
    public void updateDiaryTest(){
        MultipartFile example2 = new MockMultipartFile(
                "imageFile2",
                "test_image2.jpg",
                "image2/jpeg",
                "두 번째 가짜 이미지 데이터입니다".getBytes()
        );
        DiaryRequestDto diaryRequestDto = new DiaryRequestDto("to be updated", "will be updated", "SAD", example);
        DiaryRequestDto updatedDiaryRequestDto = new DiaryRequestDto("updated Title", "updated content", "HAPPY", example2);
        DiaryResponseDto diaryResponseDto = diaryService.joinDiary(diaryRequestDto, "tester");
        DiaryResponseDto updatedResponse = diaryService.updateDiaryById(diaryResponseDto.getId(), updatedDiaryRequestDto);
        assertThat(diaryService.findById(diaryResponseDto.getId()))
                .extracting("id", "title", "content", "mood", "imgPath")
                .containsExactly(diaryResponseDto.getId(), updatedDiaryRequestDto.getTitle(), updatedDiaryRequestDto.getContent(), updatedDiaryRequestDto.getMood(), updatedResponse.getImgPath());
    }

    @Test
    public void deleteDiaryTest(){
        DiaryRequestDto diaryRequestDto = new DiaryRequestDto("new Title", "new content", "TIRED", example);
        DiaryResponseDto diaryResponseDto = diaryService.joinDiary(diaryRequestDto, "tester");
        Diary diary = diaryService.findById(diaryResponseDto.getId());
        diaryService.deleteDiaryById(diaryResponseDto.getId());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,()-> diaryService.findById(diaryResponseDto.getId()));
        assertThat(e.getMessage()).isEqualTo("해당 일기가 존재하지 않습니다. id: " + diaryResponseDto.getId());
    }

}
