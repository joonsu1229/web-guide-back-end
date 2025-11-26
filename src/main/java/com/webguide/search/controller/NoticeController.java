package com.webguide.search.controller;

import com.webguide.search.dto.NoticeCategoryDto;
import com.webguide.search.dto.NoticeDto;
import com.webguide.search.service.NoticeCategoryService;
import com.webguide.search.service.NoticeSearchService;
import com.webguide.search.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeSearchService noticeSearchService;
     private final NoticeCategoryService noticeCategoryService;

    @GetMapping
    public List<NoticeDto> getNoticeList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        return noticeService.getNoticeList(category, keyword);
    }

    @GetMapping("/{id}")
    public NoticeDto getNoticeDetail(@PathVariable Long id) {
        noticeService.increaseViews(id);
        return noticeService.getNoticeDetail(id);
    }

    @PostMapping
    public NoticeDto createNotice(@RequestBody NoticeDto dto) {
        return noticeService.createNotice(dto);
    }

    @PutMapping("/{id}")
    public NoticeDto updateNotice(@PathVariable Long id, @RequestBody NoticeDto dto) {
        return noticeService.updateNotice(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
    }

    @GetMapping("/search")
    public List<NoticeDto> searchNotices(
            @RequestParam String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String portalId
    ) {
        if (portalId != null && !portalId.isEmpty()) {
            return noticeSearchService.searchByPortal(portalId, keyword);
        }

        if (category != null && !category.equals("all")) {
            return noticeSearchService.search(category, keyword);
        }

        return noticeSearchService.search(keyword);
    }

    @GetMapping("/categories")
    public List<NoticeCategoryDto> getCategories() {
        return noticeCategoryService.getAllCategories();
    }

}
