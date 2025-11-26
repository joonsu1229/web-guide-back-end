package com.webguide.search.service.impl;

import com.webguide.search.dto.NoticeDto;
import com.webguide.search.dto.NoticeSearchResult;
import com.webguide.search.service.NoticeSearchService;
import com.webguide.search.repository.NoticeSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeSearchServiceImpl implements NoticeSearchService {

    private final NoticeSearchRepository repository;

    @Override
    public List<NoticeDto> search(String keyword) {
        return convert(repository.searchAll(keyword, 50));
    }

    @Override
    public List<NoticeDto> search(String categoryKey, String keyword) {
        return convert(repository.searchByCategory(categoryKey, keyword, 50));
    }

    @Override
    public List<NoticeDto> searchByPortal(String portalId, String keyword) {
        return convert(repository.searchByPortal(portalId, keyword, 50));
    }

    private List<NoticeDto> convert(List<NoticeSearchResult> list) {
        return list.stream().map(r ->
                NoticeDto.builder()
                        .noticeId(r.getId())
                        .category(r.getCategory())
                        .title(r.getTitle())
                        .summary(r.getSummary())
                        .content(r.getContent())
                        .views(r.getViews())
                        .useYn(r.getUseYn())
                        .isNew(r.getIsNew())
                        .regDt(r.getRegDt())
                        .modDt(r.getModDt())
                        .build()
        ).collect(Collectors.toList());
    }
}

