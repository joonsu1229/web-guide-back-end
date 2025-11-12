package com.webguide.search.service;

import com.webguide.search.dto.CategoryDto;
import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategoryTreeBySection(String portalId, String section);
    CategoryDto createCategory(CategoryDto categoryDto);
    CategoryDto updateCategory(String portalId, Long categoryId, CategoryDto categoryDto);
    void deactivateCategoryAndChildren(String portalId, Long categoryId);
    void deleteCategory(String portalId, Long categoryId);
    List<CategoryDto> getAllCategoriesByPortal(String portalId);
}