package com.webguide.search.dto;

import com.webguide.search.entity.Document;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SearchResult {
    private Document document;

    /**
     * Hibernate가 Native Query 결과를 DTO로 직접 매핑하기 위한 생성자.
     * SELECT 순서와 정확히 일치해야 함.
     */
    public SearchResult(Long id, Long guideId, Long categoryId, Integer version, String contentBody, String createdAt) {
        this.document = new Document();
        this.document.setId(id);
        this.document.setGuideId(guideId);
        this.document.setCategoryId(categoryId);
        this.document.setVersion(version);
        this.document.setContentBody(contentBody);

        // createdAt 문자열을 LocalDateTime으로 변환
        if (createdAt != null) {
            this.document.setCreatedAt(
                LocalDateTime.parse(createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            );
        }
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
