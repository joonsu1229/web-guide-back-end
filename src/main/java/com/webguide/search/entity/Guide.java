package com.webguide.search.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "guides", schema = "webguide")
@Getter
@Setter
public class Guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", unique = true)
    private Category category;

    @Column(name = "portal_id", nullable = false)
    private String portalId;

    // 현재 활성화된 버전을 가리킴
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private GuideVersion currentVersion;

    @Column(name = "delete_yn", nullable = false)
    private boolean deleteYn = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "Guide{" +
               "id=" + id +
               ", portalId='" + portalId + '\'' +
               '}';
    }
}