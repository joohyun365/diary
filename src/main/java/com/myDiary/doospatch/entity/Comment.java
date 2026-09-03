package com.myDiary.doospatch.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    @ManyToOne
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @Builder
    public Comment(String content, Member member, Diary diary) {
        this.content = content;
        this.member = member;
        this.diary = diary;
    }
    public void updateComment(String content){
        this.content= content;
    }
}
