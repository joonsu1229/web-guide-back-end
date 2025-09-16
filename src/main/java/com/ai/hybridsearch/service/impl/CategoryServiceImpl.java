package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.dto.CategoryDto;
import com.ai.hybridsearch.entity.Category;
import com.ai.hybridsearch.repository.CategoryRepository;
import com.ai.hybridsearch.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoryTreeBySection(String portalId, String section) {
        // 수정한 Repository 메서드 이름으로 호출.
        return categoryRepository.findTopLevelActiveCategories(portalId, section)
                .stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category parent = null;
        int depth = 1;
        String portalId = categoryDto.getPortalId();

        if (categoryDto.getParentId() != null) {
            parent = categoryRepository.findByIdAndPortalId(categoryDto.getParentId(), portalId)
                    .orElseThrow(() -> new EntityNotFoundException("부모 카테고리 조회 실패: " + categoryDto.getParentId()));
            depth = parent.getDepth() + 1;
        }

        Category newCategory = new Category();
        newCategory.setName(categoryDto.getName());
        newCategory.setDescription(categoryDto.getDescription());
        newCategory.setParent(parent);
        newCategory.setDepth(depth);
        newCategory.setDisplayOrder(categoryDto.getDisplayOrder());
        newCategory.setSection(categoryDto.getSection());
        newCategory.setPortalId(portalId);
        newCategory.setActive(true);

        return CategoryDto.fromEntity(categoryRepository.save(newCategory));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(String portalId, Long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findByIdAndPortalId(categoryId, portalId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 카테고리 조회 실패: " + categoryId));

        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setDisplayOrder(categoryDto.getDisplayOrder());

        return CategoryDto.fromEntity(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deactivateCategoryAndChildren(String portalId, Long categoryId) {
        Category category = categoryRepository.findByIdAndPortalId(categoryId, portalId)
                .orElseThrow(() -> new EntityNotFoundException("비활성화할 카테고리 조회 실패: " + categoryId));

        // 1. 비활성화할 모든 카테고리 ID를 재귀적으로 수집.
        List<Long> idsToDeactivate = new ArrayList<>();
        collectAllChildIds(category, idsToDeactivate);

        // 2. 수집된 ID 목록을 사용해 한 번의 쿼리로 비활성화.
        if (!idsToDeactivate.isEmpty()) {
            categoryRepository.deactivateByIds(idsToDeactivate, portalId);
        }
    }

    /**
     * 자신과 모든 자식 카테고리의 ID를 재귀적으로 리스트에 추가하는 헬퍼 메서드.
     */
    private void collectAllChildIds(Category category, List<Long> idList) {
        idList.add(category.getId());
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            for (Category child : category.getChildren()) {
                collectAllChildIds(child, idList);
            }
        }
    }

    @Override
    @Transactional
    public void deleteCategory(String portalId, Long categoryId) {
        Category category = categoryRepository.findByIdAndPortalId(categoryId, portalId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 카테고리 조회 실패: " + categoryId));
        categoryRepository.delete(category);
    }

    /**
     * 특정 포탈 내에서 모든 카테고리 조회 (활성 여부 무관)
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategoriesByPortal(String portalId) {
        return categoryRepository.findAllByPortalId(portalId).stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

}