package com.ai.hybridsearch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Table(name = "guide_versions", schema = "webguide")
@Getter
@Setter
public class GuideVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_body", columnDefinition = "text")
    private String contentBody;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;

    @Column(nullable = false)
    private int version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // vector 검색을 위한 vector컬럼
    @Column(name = "embedding", columnDefinition = "vector(768)")
    @Transient
    private float[] embedding;

    @Override
    public String toString() {
        return "GuideVersion{" +
               "id=" + id +
               ", version=" + version +
               '}';
    }
}