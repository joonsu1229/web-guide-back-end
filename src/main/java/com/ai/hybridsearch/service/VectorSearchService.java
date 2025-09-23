package com.ai.hybridsearch.service;

import com.ai.hybridsearch.dto.SearchResult;
import com.ai.hybridsearch.entity.Document;
import java.util.List;

public interface VectorSearchService {
    /**
     * 쿼리 문장을 벡터로 변환하여 DB에 저장된 벡터와 비교해 의미적으로 유사한 문서
     * @param query 사용자 검색어
     * @param category 검색할 카테고리 (null일 경우 전체 검색)
     * @param limit 반환할 결과 수
     * @return 검색 결과 리스트
     */
    List<SearchResult> semanticSearch(String query, String category, int limit);

    /**
     * Full-text search를 수행하여 검색어와 일치하는 문서를
     * @param searchQuery 처리된 full-text 검색 쿼리
     * @param category 검색할 카테고리 (null일 경우 전체 검색)
     * @param limit 반환할 결과 수
     * @return 검색된 문서 리스트
     */
    List<Document> findByFullTextSearch(String searchQuery, String category, int limit);
}