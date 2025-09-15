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

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam String portalId,
            @RequestParam String section) {
        return ResponseEntity.ok(categoryService.getCategoryTreeBySection(portalId, section));
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
}