package com.webguide.search.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDto {

    private Long noticeId;

    // 공지 카테고리 (update / event / service ...)
    private String category;

    private String title;    // 제목
    private String summary;  // 요약
    private String content;  // HTML 본문

    private Integer views;   // 조회수
    private String useYn;    // 사용 여부 (Y/N)
    private String isNew;    // 신규 여부 표시 (Y/N 등)

    private LocalDateTime regDt; // 등록일
    private LocalDateTime modDt; // 수정일
}
