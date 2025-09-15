package com.ai.hybridsearch.dto;

import com.ai.hybridsearch.entity.GuideVersion;
import lombok.Builder;
import lombok.Getter;

// GuideContentDto.java
@Getter
@Builder
public class GuideContentDto {
    private Long id; // 버전 ID
    private String contentBody;
    private int version;
    private Long categoryId;

    public static GuideContentDto fromEntity(GuideVersion entity) {
        return GuideContentDto.builder()
                .id(entity.getId())
                .contentBody(entity.getContentBody())
                .version(entity.getVersion())
                .categoryId(entity.getGuide().getCategory().getId())
                .build();
    }
}