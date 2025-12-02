package com.webguide.search.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "guide_versions", schema = "webguide") // 실제 테이블명인 guide_versions에 매핑
@Getter
@Setter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // guide_id 필드 추가
    @Column(name = "guide_id")
    private Long guideId;

    // version 필드 추가
    @Column(name = "version")
    private int version;

    @Column(name = "category_id")
    private Long categoryId;

    // content_body 필드명 유지
    @Column(name = "content_body", columnDefinition = "text")
    private String contentBody;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Document() {}
}