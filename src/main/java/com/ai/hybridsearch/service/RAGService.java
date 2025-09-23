package com.ai.hybridsearch.service;

import com.ai.hybridsearch.dto.SearchResult;
import com.ai.hybridsearch.entity.Document;
import lombok.Data;
import java.util.List;

public interface RAGService {

    @Data
    class GeneratedResponse {
        private String answer;
        private List<Document> sources;
    }

    /**
     * 검색된 컨텍스트와 원본 질문을 기반으로 최종 답변을 생성
     * @param originalQuery 사용자의 원본 질문
     * @param contextDocs 검색된 관련 문서 목록
     * @return 생성된 답변과 출처 문서가 포함된 객체
     */
    GeneratedResponse generate(String originalQuery, List<SearchResult> contextDocs);
}