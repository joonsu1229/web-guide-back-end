package com.ai.hybridsearch.dto;

import lombok.Getter;

@Getter
public class GuideContentDto {
    private Long id; // 버전 ID
    private String contentBody;
    private int version;
    private Long categoryId;
    private String menu;
    private String title;
    private String description;

    // JPQL의 "SELECT new" 구문이 사용할 생성자
    public GuideContentDto(Long id, String contentBody, int version, Long categoryId) {
        this.id = id;
        this.contentBody = contentBody;
        this.version = version;
        this.categoryId = categoryId;
    }

    public static GuideContentDto empty(Long categoryId) {
        return new GuideContentDto(null, "", 0, categoryId);
    }
}