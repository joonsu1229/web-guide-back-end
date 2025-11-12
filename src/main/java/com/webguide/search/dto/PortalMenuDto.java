package com.webguide.search.dto;

import com.webguide.search.entity.PortalMenu;
import lombok.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalMenuDto {

    private Long id;
    private String title;
    private String description;
    private String icon;
    private String className;
    private List<String> tags;
    private String section;

    public static PortalMenuDto fromEntity(PortalMenu entity) {
        List<String> tagList = entity.getTags() != null && !entity.getTags().isEmpty()
                ? Arrays.asList(entity.getTags().split(","))
                : Collections.emptyList();

        return PortalMenuDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .icon(entity.getIcon())
                .className(entity.getClassName())
                .tags(tagList)
                .section(entity.getSection())
                .build();
    }
}