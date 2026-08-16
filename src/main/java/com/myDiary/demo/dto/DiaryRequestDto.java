package com.myDiary.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class DiaryRequestDto {
    @NotBlank(message = "제목은 필수!")
    private String title;

    @NotBlank(message = "내용을 적으세요.")
    @Size(min = 10, message = "일기 본문은 최소 10자 이상 적어주세요.")
    private String content;
    private String mood;
    private MultipartFile imageFile;

    public DiaryRequestDto(String title, String content, String mood, MultipartFile imageFile) {
        this.title = title;
        this.content = content;
        this.mood = mood;
        this.imageFile = imageFile;
    }
}
