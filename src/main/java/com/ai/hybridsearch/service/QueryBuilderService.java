package com.ai.hybridsearch.service;

import lombok.Data;

public interface QueryBuilderService {

    /**
     * 쿼리 분석 결과를 담을 DTO.
     * 인터페이스 내에 static 중첩 클래스로 두거나 별도 DTO 패키지로 분리
     */
    @Data
    class TransformedQuery {
        private String lexicalQuery;
        private String semanticQuery;
        private String originalQuery;
    }

    /**
     * 사용자 쿼리를 분석하고 검색에 최적화된 형태로 변환
     * @param userQuery 사용자의 원본 질문
     * @return 변환된 쿼리 객체
     */
    TransformedQuery transformQuery(String userQuery);
}