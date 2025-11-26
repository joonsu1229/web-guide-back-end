package com.webguide.search.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Notice 검색 결과 DTO (NativeQuery 생성자 매핑 방식)
 * Guide SearchResult와 동일한 구조
 */
@Getter
@Setter
public class NoticeSearchResult {

    private Long id;
    private String category;
    private String title;
    private String summary;
    private String content;
    private Integer views;
    private String useYn;
    private String isNew;

    private LocalDateTime regDt;
    private LocalDateTime modDt;

    /**
     * NativeQuery 생성자 매핑용
     * SELECT 순서와 정확하게 일치해야 함
     */
    public NoticeSearchResult(
            Long id,
            String category,
            String title,
            String summary,
            String content,
            Integer views,
            String useYn,
            String isNew,
            String regDt,
            String modDt
    ) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.views = views;
        this.useYn = useYn;
        this.isNew = isNew;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        if (regDt != null) {
            try { this.regDt = LocalDateTime.parse(regDt, formatter); }
            catch (Exception ignored) {}
        }

        if (modDt != null) {
            try { this.modDt = LocalDateTime.parse(modDt, formatter); }
            catch (Exception ignored) {}
        }
    }
}
