package com.webguide.search.service.impl;

import com.webguide.search.dto.NoticeDto;
import com.webguide.search.entity.Notice;
import com.webguide.search.repository.NoticeRepository;
import com.webguide.search.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository repo;

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDto> getNoticeList(String category, String keyword) {

        List<Notice> notices;

        if (category != null && !category.equals("all")) {
            notices = repo.findAllByCategoryAndUseYn(category, "Y");
        } else {
            notices = repo.findAllByUseYn("Y");
        }

        if (keyword != null && !keyword.isEmpty()) {
            String lower = keyword.toLowerCase();
            notices = notices.stream()
                    .filter(n ->
                            (n.getTitle() != null && n.getTitle().toLowerCase().contains(lower)) ||
                            (n.getSummary() != null && n.getSummary().toLowerCase().contains(lower))
                    )
                    .collect(Collectors.toList());
        }

        return notices.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDto getNoticeDetail(Long noticeId) {
        Notice notice = repo.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        return toDto(notice);
    }

    @Override
    public void increaseViews(Long noticeId) {
        Notice notice = repo.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        notice.setViews(notice.getViews() + 1);
        repo.save(notice);
    }

    @Override
    public NoticeDto createNotice(NoticeDto form) {
        Notice notice = toEntity(form);
        Notice saved = repo.save(notice);
        return toDto(saved);
    }

    @Override
    public NoticeDto updateNotice(Long noticeId, NoticeDto form) {
        Notice notice = repo.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        notice.setCategory(form.getCategory());
        notice.setTitle(form.getTitle());
        notice.setSummary(form.getSummary());
        notice.setContent(form.getContent());
        notice.setUseYn(form.getUseYn());
        notice.setIsNew(form.getIsNew());

        Notice saved = repo.save(notice);
        return toDto(saved);
    }

    @Override
    public void deleteNotice(Long noticeId) {
        repo.deleteById(noticeId);
    }

    private NoticeDto toDto(Notice n) {
        return NoticeDto.builder()
                .noticeId(n.getNoticeId())
                .category(n.getCategory())
                .title(n.getTitle())
                .summary(n.getSummary())
                .content(n.getContent())
                .views(n.getViews())
                .useYn(n.getUseYn())
                .isNew(n.getIsNew())
                .regDt(n.getRegDt())
                .modDt(n.getModDt())
                .build();
    }

    private Notice toEntity(NoticeDto d) {
        return Notice.builder()
                .category(d.getCategory())
                .title(d.getTitle())
                .summary(d.getSummary())
                .content(d.getContent())
                .views(d.getViews() != null ? d.getViews() : 0)
                .useYn(d.getUseYn() != null ? d.getUseYn() : "Y")
                .isNew(d.getIsNew() != null ? d.getIsNew() : "N")
                .build();
    }
}
