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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String username;
    private String email;
    private String password;
    private String auth;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Diary> diaryList=new ArrayList<>();
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList=new ArrayList<>();

    @Builder
    public Member(String name, String username, String email, String password, String auth) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.auth = auth;
    }

    public void addDiary(Diary diary) {
        this.diaryList.add(diary);
    }
    public void addComment(Comment comment) {
        this.commentList.add(comment);
    }
}
