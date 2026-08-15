package com.myDiary.demo.service;

import com.myDiary.demo.dto.DiaryRequestDto;
import com.myDiary.demo.dto.DiaryResponseDto;
import com.myDiary.demo.entity.Diary;
import com.myDiary.demo.entity.Member;
import com.myDiary.demo.repository.DiaryRepository;

import com.myDiary.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 전체에 기본적으로 조회용 트랜잭션 적용
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public DiaryResponseDto joinDiary(DiaryRequestDto diaryRequestDto, String username) { // diary 생성
        Member member = memberRepository.findByUsername(username).orElseThrow(
                ()->new IllegalArgumentException("없는 유저입니다.")
        );
        Diary diary = Diary.builder()
                .title(diaryRequestDto.getTitle())
                .content(diaryRequestDto.getContent())
                .mood(diaryRequestDto.getMood())
                .member(member)
                .build();
        diaryRepository.save(diary);
        return new DiaryResponseDto(diary);
    }

    public DiaryResponseDto getDiaryById(Long id) {
        Diary diary = findById(id);
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public DiaryResponseDto updateDiaryById(Long id, DiaryRequestDto diaryRequestDto){
        Diary diary = findById(id);
        diary.updateDiary(diaryRequestDto.getTitle(), diaryRequestDto.getContent(), diaryRequestDto.getMood());
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public void deleteDiaryById(Long id){
        Diary diary = findById(id);
        diaryRepository.delete(diary);
    }

    public Diary findById(Long id) {
        return diaryRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("해당 일기가 존재하지 않습니다. id: " + id));
    }

    public List<DiaryResponseDto> findAll(){
        List<Diary> diaryList = diaryRepository.findAll();
        return diaryList.stream()
                .map(diary -> new DiaryResponseDto(diary))
                .toList();
    }

}
