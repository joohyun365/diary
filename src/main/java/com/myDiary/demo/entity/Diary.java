package com.myDiary.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지 + ID 변조 방지
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition ="TEXT")
    private String content;
    private String mood;
    private String imgPath;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList=new ArrayList<>();

    @Builder
    public Diary(String title, String content, String mood, Member member, String imgPath) {
        this.title = title;
        this.content = content;
        this.mood = mood;
        this.member = member;
        this.imgPath = imgPath;
    }

    public void updateDiary(String title, String content, String mood, String imgPath){
        this.title = title;
        this.content = content;
        this.mood = mood;
        this.imgPath = imgPath;
    }
    public void addComment(Comment comment) {
        this.commentList.add(comment);
    }
}
