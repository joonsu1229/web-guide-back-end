package com.webguide.search.service;

import com.webguide.search.dto.NoticeDto;

import java.util.List;

public interface NoticeSearchService {

    List<NoticeDto> search(String keyword);

    List<NoticeDto> search(String categoryKey, String keyword);

    List<NoticeDto> searchByPortal(String portalId, String keyword);
}
