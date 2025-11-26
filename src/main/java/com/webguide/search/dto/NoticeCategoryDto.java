package com.webguide.search.dto;

import com.webguide.search.entity.NoticeCategory;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeCategoryDto {

    private Long id;
    private String key;
    private String label;
    private String iconKey;

    public static NoticeCategoryDto fromEntity(NoticeCategory c) {
        return new NoticeCategoryDto(
                c.getId(),
                c.getCategoryKey(),
                c.getCategoryName(),
                c.getIconKey()
        );
    }
}
