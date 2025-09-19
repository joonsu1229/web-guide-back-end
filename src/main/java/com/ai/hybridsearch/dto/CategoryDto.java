package com.ai.hybridsearch.dto;

import com.ai.hybridsearch.entity.Category;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;
    private String name;
    private String description;
    private int depth;
    private Integer displayOrder;
    private String section;
    private List<CategoryDto> children;

    // 요청 시 사용될 필드
    private Long parentId;
    private String portalId;


    public static CategoryDto fromEntity(Category entity) {
        return CategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .depth(entity.getDepth())
                .displayOrder(entity.getDisplayOrder())
                .section(entity.getSection())
                .parentId (entity.getParent() != null ? entity.getParent().getId() : null)
                .portalId(entity.getPortalId())
                .children(
                    entity.getChildren() != null
                        ? entity.getChildren().stream().map(CategoryDto::fromEntity).collect(Collectors.toList())
                        : Collections.emptyList()
                )
                .build();
    }
}