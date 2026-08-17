package com.myDiary.demo.controller;

import com.myDiary.demo.dto.DiaryRequestDto;
import com.myDiary.demo.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor // final 붙은 필드 생성자 자동 생성
@RequestMapping("/diaries")
public class DiaryController {
    private final DiaryService diaryService;

    @GetMapping
    public String getDiaryList(Model model) {
        model.addAttribute("diaries", diaryService.findAll());
        return "diary/list";
    }
    @GetMapping("/{id}")
    public String getDiaryDetail(@PathVariable Long id, Model model) {
        model.addAttribute("diary", diaryService.getDiaryById(id));
        return "diary/detail";
    }
    @GetMapping("/new")
    public String getNewDiary() {
        return "diary/form";
    }
    @PostMapping
    public String addDiary(@Valid @ModelAttribute DiaryRequestDto diaryRequestDto, BindingResult bindingResult,
                           @AuthenticationPrincipal UserDetails userDetails){
        if (bindingResult.hasErrors()) {
            return "diary/form";
        }
        diaryService.joinDiary(diaryRequestDto, userDetails.getUsername());
        return "redirect:/diaries"; // 저장 완료하고 일기 목록 화면으로 이동
    }
    @PutMapping("/{id}/edit")
    public String modifyDiary(@PathVariable Long id, @Valid DiaryRequestDto diaryRequestDto, BindingResult bindingResult,
                              @AuthenticationPrincipal UserDetails userDetails){
        if (bindingResult.hasErrors()){
            return "diary/form";
        }
        diaryService.updateDiaryById(id, diaryRequestDto, userDetails.getUsername());
        return "redirect:/diaries/" + id;
    }

    @DeleteMapping("/{id}/delete")
    public String deleteDiary(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails){
        diaryService.deleteDiaryById(id, userDetails.getUsername());
        return "redirect:/diaries";
    }

    @GetMapping("/{id}/edit")
    public String goToModifyDiary(@PathVariable Long id, Model model) {
        model.addAttribute("diary", diaryService.getDiaryById(id));
        return "diary/form";
    }
}
