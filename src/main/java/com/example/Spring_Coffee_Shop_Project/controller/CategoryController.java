package com.example.Spring_Coffee_Shop_Project.controller;

import com.example.Spring_Coffee_Shop_Project.constant.CommonResponse;
import com.example.Spring_Coffee_Shop_Project.dto.CategoryDTO;
import com.example.Spring_Coffee_Shop_Project.enumeration.CategoryStatus;
import com.example.Spring_Coffee_Shop_Project.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CommonResponse> saveCategory (@RequestBody CategoryDTO categoryDTO){
        categoryService.saveCategory(categoryDTO);
        return ResponseEntity.ok(new CommonResponse(200,"Category Saved Successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse> updateCategory (@PathVariable Long id,@RequestBody CategoryDTO categoryDTO){
        categoryService.updateCategory(id,categoryDTO);
        return ResponseEntity.ok(new CommonResponse(200,"Category Updated Successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse> deleteCategory (@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new CommonResponse(200,"Category status updated to INACTIVE successfully"));
    }

    // Get Category By ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // Frontend CategoryStatuses Endpoint
    @GetMapping("/statuses")
    public ResponseEntity<CategoryStatus[]> getCategoryStatuses() {
        return ResponseEntity.ok(CategoryStatus.values());
    }
}
