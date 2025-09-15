package com.ai.hybridsearch.service;

import com.ai.hybridsearch.dto.GuideContentDto;
import java.util.List;

public interface GuideContentService {

    /**
     * 특정 카테고리에 연결된 가이드의 '현재 활성화된 버전'을 조회.
     */
    GuideContentDto getCurrentContent(String portalId, Long categoryId);

    /**
     * 특정 카테고리에 연결된 가이드의 '새로운 버전'을 저장.
     * 기존 Guide가 없으면 새로 생성함.
     */
    GuideContentDto saveNewVersion(String portalId, Long categoryId, String contentBody);

    /**
     * 특정 카테고리에 연결된 가이드를 소프트 삭제 처리.
     */
    void softDeleteGuide(String portalId, Long categoryId);

    /**
     * 특정 카테고리에 연결된 가이드의 전체 버전 기록을 조회.
     */
    List<GuideContentDto> getVersionHistory(String portalId, Long categoryId);
}