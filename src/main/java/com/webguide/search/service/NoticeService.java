package com.webguide.search.service;

import com.webguide.search.dto.NoticeDto;

import java.util.List;

public interface NoticeService {

    List<NoticeDto> getNoticeList(String category, String keyword);

    NoticeDto getNoticeDetail(Long noticeId);

    NoticeDto createNotice(NoticeDto form);

    NoticeDto updateNotice(Long noticeId, NoticeDto form);

    void deleteNotice(Long noticeId);

    void increaseViews(Long noticeId);
}
