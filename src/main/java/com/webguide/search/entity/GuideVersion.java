package com.webguide.search.entity;

import jakarta.persistence.*;
import lombok.*;

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
    @Override
    public String toString() {
        return "GuideVersion{" +
               "id=" + id +
               ", version=" + version +
               '}';
    }
}