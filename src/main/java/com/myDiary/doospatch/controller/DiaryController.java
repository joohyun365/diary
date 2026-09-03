package com.myDiary.doospatch.controller;

import com.myDiary.doospatch.dto.DiaryRequestDto;
import com.myDiary.doospatch.dto.DiaryResponseDto;
import com.myDiary.doospatch.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor // final 붙은 필드 생성자 자동 생성
@RequestMapping("/api/diaries")
public class DiaryController {
    private final DiaryService diaryService;

    @GetMapping
    public ResponseEntity<List<DiaryResponseDto>> getDiaryList() {
        return ResponseEntity.status(HttpStatus.OK).body(diaryService.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponseDto> getDiaryDetail(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(diaryService.getDiaryById(id));
    }
    @PostMapping // 바이너리 데이터(이미지 파일)보낼 때 폼 데이터 형식으로 보내야 하므로 @RequestBody 빼고 @ModelAttribute로
    public ResponseEntity<DiaryResponseDto> addDiary(@Valid @ModelAttribute DiaryRequestDto diaryRequestDto,
                           @AuthenticationPrincipal UserDetails userDetails){
        DiaryResponseDto diaryResponseDto = diaryService.joinDiary(diaryRequestDto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResponseDto);
    }
    @PutMapping("/{id}")
    public ResponseEntity<DiaryResponseDto> modifyDiary(@PathVariable Long id, @Valid @ModelAttribute DiaryRequestDto diaryRequestDto,
                              @AuthenticationPrincipal UserDetails userDetails){
        DiaryResponseDto diaryResponseDto = diaryService.updateDiaryById(id, diaryRequestDto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(diaryResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails){
        diaryService.deleteDiaryById(id, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
