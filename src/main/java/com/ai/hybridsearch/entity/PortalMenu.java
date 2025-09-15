package com.ai.hybridsearch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "portal_menus", schema = "webguide")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortalMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String icon;

    @Column(name = "class_name", length = 100)
    private String className;

    @Column(length = 255)
    private String tags; // 예: '기초,튜토리얼'

    @Column(unique = true, nullable = false, length = 100)
    private String section; // Category 테이블과 연결될 고유 키

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "portal_id", nullable = false, length = 50)
    private String portalId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}