package com.myDiary.demo.service;

import java.nio.file.Files;
import com.myDiary.demo.dto.AiResponseDto;
import com.myDiary.demo.dto.DiaryRequestDto;
import com.myDiary.demo.dto.DiaryResponseDto;
import com.myDiary.demo.entity.Diary;
import com.myDiary.demo.entity.Member;
import com.myDiary.demo.repository.DiaryRepository;

import com.myDiary.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 전체에 기본적으로 조회용 트랜잭션 적용
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private  final AiService aiService;
    @Value("${file.dir}")
    private String fileDir;

    @Transactional
    public DiaryResponseDto joinDiary(DiaryRequestDto diaryRequestDto, String username) { // diary 생성
        Member member = memberRepository.findByUsername(username).orElseThrow(
                ()->new IllegalArgumentException("없는 유저입니다.")
        );

        AiResponseDto aiResponseDto = aiService.analyzeDiary(diaryRequestDto.getContent());

        String savedImagePath = saveImageFile(diaryRequestDto.getImageFile());

        Diary diary = Diary.builder()
                .title(diaryRequestDto.getTitle())
                .content(diaryRequestDto.getContent())
                .mood(aiResponseDto.analyzedMood()) // AI가 분석한 mood
                .member(member)
                .imgPath(savedImagePath)
                .build();
        diaryRepository.save(diary);
        member.addDiary(diary);
        return new DiaryResponseDto(diary);
    }

    public DiaryResponseDto getDiaryById(Long id) {
        Diary diary = findById(id);
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public DiaryResponseDto updateDiaryById(Long id, DiaryRequestDto diaryRequestDto, String currentUsername){
        Diary diary = findById(id);
        validateAuthor(diary, currentUsername);
        String imgPath=diary.getImgPath();
        MultipartFile newImageFile=diaryRequestDto.getImageFile();
        if(newImageFile!=null && !newImageFile.isEmpty()) {
            deleteImageFile(diary.getImgPath());
            imgPath = saveImageFile(diaryRequestDto.getImageFile());
        }
        diary.updateDiary(diaryRequestDto.getTitle(), diaryRequestDto.getContent(), diaryRequestDto.getMood(), imgPath);
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public void deleteDiaryById(Long id, String currentUsername){
        Diary diary = findById(id);
        validateAuthor(diary, currentUsername);
        deleteImageFile(diary.getImgPath());
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
    private String saveImageFile(MultipartFile imageFile){
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        // 난수 생성
        // 유저들이 같은 이름의 사진을 올리면 덮여 쓰이는 것 방지
        String originalFilename = Path.of(imageFile.getOriginalFilename())
                .getFileName()
                .toString();
        String savedFileName = UUID.randomUUID().toString() + "_" + originalFilename;

        Path rootPath = Path.of(fileDir)
                .toAbsolutePath()
                .normalize();
        Path savePath = rootPath // 운영체제에 맞게 안전한 경로 객체로 만듦
                .resolve(savedFileName) // 뒤에 파일명 붙이기(기준 폴더 + 저장할 파일명)
                .normalize(); // 불필요한 . .. 정리함.

        if(!savePath.startsWith(rootPath)){ // 이중 확인용
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
        // 해당 경로에 빈 파일을 생성하고 그곳에 첨부파일을 덮어쓰기
        try {
            Files.createDirectories(rootPath); // 부모 경로에 폴더 없으면 만듦
            imageFile.transferTo(savePath); //MultipartFile 데이터를 savePath에 저장
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 저장에 실패했습니다.", e);
        }
        // DB에 저장할 웹 접근 경로 (브라우저가 이 주소로 접근함)
        return "/images/" + savedFileName;
    }

    private void deleteImageFile(String imgPath){
        if (imgPath==null ||imgPath.isEmpty())return;
        String fileName = Path.of(imgPath)
                .getFileName()
                .toString();
        Path rootPath = Path.of(fileDir)
                .toAbsolutePath()
                .normalize();
        Path imagePath = rootPath
                .resolve(fileName)
                .normalize();
        if(!imagePath.startsWith(rootPath)){ // 이미 파일명만 추출했지만 이중 확인용
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 삭제에 실패했습니다", e);
        }
    }

    public void deleteAllImageFilesByMember(Member member) {
        List<Diary> diaryList = member.getDiaryList();
        for (Diary diary : diaryList) {
            if (diary.getImgPath()!=null) {
                deleteImageFile(diary.getImgPath());
            }
        }
    }

    private void validateAuthor(Diary diary, String currentUsername) {
        if (!diary.getMember().getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("작성자 본인만 접근할 수 있습니다.");
        }
    }
}
