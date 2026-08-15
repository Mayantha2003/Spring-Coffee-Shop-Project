package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {

    CategoryDTO saveCategory(CategoryDTO categoryDto);

    CategoryDTO updateCategory(long id, CategoryDTO categoryDto);

    void deleteCategory(long id);

    CategoryDTO getCategoryById(long id);

    List<CategoryDTO> getAllCategories();
}