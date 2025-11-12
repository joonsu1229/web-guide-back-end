package com.webguide.search.controller;

import com.webguide.search.dto.GuideContentDto;
import com.webguide.search.service.GuideContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// GuideContentController.java
@RestController
@RequestMapping("/api/contents")
@CrossOrigin(origins = "*")
public class GuideContentController {

    private final GuideContentService guideContentService;

    public GuideContentController(GuideContentService guideContentService) {
        this.guideContentService = guideContentService;
    }

    // 현재 버전 조회
    @GetMapping
    public ResponseEntity<GuideContentDto> getContent(@RequestParam String portalId, @RequestParam Long categoryId) {
        return ResponseEntity.ok(guideContentService.getCurrentContent(portalId, categoryId));
    }

    // 새 버전 저장 (수정)
    @PostMapping
    public ResponseEntity<GuideContentDto> saveContent(@RequestBody GuideContentDto requestDto) {
        return ResponseEntity.ok(guideContentService.saveNewVersion(
            "P1", // 실제로는 JWT 등에서 portalId를 가져와야 함 나중에 포탈아이디 dto든 추가해야댐
            requestDto
        ));
    }

    // 소프트 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteContent(@RequestParam String portalId, @RequestParam Long categoryId) {
        guideContentService.softDeleteGuide(portalId, categoryId);
        return ResponseEntity.ok().build();
    }
}