package com.webguide.search.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notice_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카테고리 key (update / event / service …)
    @Column(name = "category_key", nullable = false, unique = true)
    private String categoryKey;

    // 화면에 표시될 이름
    @Column(name = "category_name", nullable = false)
    private String categoryName;

    // Vue에서 아이콘 선택용 (flash / calendar / info ...)
    @Column(name = "icon_key", nullable = false)
    private String iconKey;

    @Column(name = "use_yn", nullable = false)
    private String useYn = "Y";

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
