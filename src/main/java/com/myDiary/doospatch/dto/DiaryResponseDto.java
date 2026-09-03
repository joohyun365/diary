package com.myDiary.doospatch.dto;

import com.myDiary.doospatch.entity.Diary;
import lombok.Getter;

@Getter
public class DiaryResponseDto {
    private Long id;
    private String title;
    private String content;
    private String mood;
    private String imgPath;

    public DiaryResponseDto(Diary diary) {
        this.id = diary.getId();
        this.title = diary.getTitle();
        this.content = diary.getContent();
        this.mood = diary.getMood();
        this.imgPath = diary.getImgPath();
    }
}
