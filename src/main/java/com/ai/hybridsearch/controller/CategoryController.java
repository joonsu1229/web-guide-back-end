package com.ai.hybridsearch.controller;

import com.ai.hybridsearch.dto.CategoryDto;
import com.ai.hybridsearch.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 목록 조회 (section 파라미터 있으면 섹션별 / 없으면 전체 카테고리 반환)
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam String portalId,
            @RequestParam(required = false) String section) {

        if (section != null) {
            return ResponseEntity.ok(categoryService.getCategoryTreeBySection(portalId, section));
        } else {
            return ResponseEntity.ok(categoryService.getAllCategoriesByPortal(portalId));
        }
    }


    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@RequestParam String portalId,
                                                      @PathVariable("id") Long categoryId,
                                                      @RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.updateCategory(portalId, categoryId, categoryDto));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(@RequestParam String portalId,
                                                   @PathVariable("id") Long categoryId) {
        categoryService.deactivateCategoryAndChildren(portalId, categoryId);
        return ResponseEntity.ok().build();
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@RequestParam String portalId,
                                                 @PathVariable("id") Long categoryId) {
        categoryService.deleteCategory(portalId, categoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 포탈 내에서 모든 카테고리 조회 (활성 여부 무관)
     */
    @GetMapping("/all")
    public ResponseEntity<List<CategoryDto>> getAllCategories(@RequestParam String portalId) {
        return ResponseEntity.ok(categoryService.getAllCategoriesByPortal(portalId));
    }
}