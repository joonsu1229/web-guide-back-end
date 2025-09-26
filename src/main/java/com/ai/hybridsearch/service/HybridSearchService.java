package com.ai.hybridsearch.service;

import com.ai.hybridsearch.dto.SearchResult;
import java.util.List;

public interface HybridSearchService {
    /**
     * 어휘 검색과 의미 검색을 결합하여 가장 연관성 높은 결과를 반환
     */
    List<SearchResult> hybridSearch(String query, String category, int limit);

    /**
     * PostgreSQL의 Full-text search 기능을 사용하여 어휘 기반 검색을 수행
     */
    List<SearchResult> lexicalSearch(String query, String category, int limit);
    /**
     * 하이브리드 검색에 구문(phrase) 및 퍼지(fuzzy) 검색 옵션을 추가하여 검색을 확장
     */
    List<SearchResult> advancedHybridSearch(String query, String category,
                                            boolean useFuzzy, boolean usePhrase, int limit);

    RAGService.GeneratedResponse searchAndGenerate(String query, String category, int limit);
}