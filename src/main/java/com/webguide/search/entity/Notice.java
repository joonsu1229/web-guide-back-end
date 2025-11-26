package com.webguide.search.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    private String category;     // 공지 카테고리
    private String title;        // 제목
    private String summary;      // 요약
    private String content;      // HTML 본문
    private Integer views;       // 조회수
    private String useYn;        // 사용 여부
    private String isNew;        // 신규 여부 표시
    private String deleteYn;     // 삭제 여부

    private LocalDateTime regDt;
    private LocalDateTime modDt;

    @PrePersist
    public void prePersist() {
        this.views = 0;
        this.useYn = "Y";
        this.isNew = "N";
        this.regDt = LocalDateTime.now();
        this.modDt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.modDt = LocalDateTime.now();
    }
}
