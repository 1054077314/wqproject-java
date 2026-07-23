package com.campus.category.controller;

import com.campus.category.dto.CategoryRequest;
import com.campus.category.entity.Category;
import com.campus.category.service.CategoryService;
import com.campus.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listSimple()));
    }

    @PostMapping("/admin/categories/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("创建成功", categoryService.create(request)));
    }

    @PutMapping("/admin/categories/{id}/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> update(@PathVariable Long id,
                                                        @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("更新成功", categoryService.update(id, request)));
    }

    @DeleteMapping("/admin/categories/{id}/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("删除成功", null));
    }
}
