package com.ai.hybridsearch.service;

import com.ai.hybridsearch.dto.SearchResult;
import java.util.List;

public interface RerankerService {

    /**
     * 검색 결과를 받아 의미적 유사도를 추가로 계산하여 재정렬(rerank)합니다.
     *
     * @param results 초기 검색 결과
     * @param query 원본 사용자 쿼리
     * @param topK 최종 반환할 결과 수
     * @return 재정렬된 최종 결과
     */
    List<SearchResult> rerank(List<SearchResult> results, String query, int topK);
}