package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.CategoryDTO;
import com.example.Spring_Coffee_Shop_Project.entity.Category;
import com.example.Spring_Coffee_Shop_Project.enumeration.CategoryStatus;
import com.example.Spring_Coffee_Shop_Project.exception.CustomerException;
import com.example.Spring_Coffee_Shop_Project.repository.CategoryRepository;
import com.example.Spring_Coffee_Shop_Project.repository.ItemRepository;
import com.example.Spring_Coffee_Shop_Project.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    @Override
    public void saveCategory(CategoryDTO categoryDto) {

        log.info("Executing Save Category method...");

        if (categoryRepository.existsByCategoryName(categoryDto.getCategoryName())) {
            throw new CustomerException(400, "Category name already exists: " + categoryDto.getCategoryName());
        }

        Category category = mapToEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);

        log.info("Category saved successfully with id: {}", savedCategory.getCategoryId());
    }

    @Override
    public void updateCategory(CategoryDTO categoryDto) {

        log.info("Executing Full Update for Category ID: {}", categoryDto.getCategoryId());

        Optional<Category> optionalCategory = categoryRepository.findById(categoryDto.getCategoryId());
        if (optionalCategory.isEmpty()) {
            throw new CustomerException(404, "Category not found with id: " + categoryDto.getCategoryId());
        }

        Category category = optionalCategory.get();
        category.setCategoryName(categoryDto.getCategoryName());
        category.setDescription(categoryDto.getDescription());
        category.setIcon(categoryDto.getIcon());
        category.setCategoryStatus(categoryDto.getCategoryStatus() != null ? categoryDto.getCategoryStatus() : CategoryStatus.ACTIVE);
        category.setDisplayOrder(categoryDto.getDisplayOrder());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", updatedCategory.getCategoryId());
    }

    @Override
    public void deleteCategory(long id) {

        log.info("Executing Soft Delete for Category ID: {}", id);

        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isEmpty()) {
            throw new CustomerException(404, "Category not found with id: " + id);
        }

        Category category = optionalCategory.get();
        category.setCategoryStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);

        log.info("Category marked as INACTIVE for id: {}", id);
    }

    @Override
    public CategoryDTO getCategoryById(long id) {

        log.info("Fetching category by ID: {}", id);

        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isEmpty()) {
            throw new CustomerException(404, "Category not found with id: " + id);
        }

        return mapToDTO(optionalCategory.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {

        log.info("Fetching all categories...");

        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOList = new ArrayList<>();

        for (Category category : categories) {
            categoryDTOList.add(mapToDTO(category));
        }
        return categoryDTOList;
    }

    private CategoryDTO mapToDTO(Category category) {
        long count = itemRepository.countByCategoryCategoryId(category.getCategoryId());
        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .icon(category.getIcon())
                .categoryStatus(category.getCategoryStatus())
                .displayOrder(category.getDisplayOrder())
                .itemCount((int) count)
                .build();
    }

    private Category mapToEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setCategoryId(dto.getCategoryId());
        category.setCategoryName(dto.getCategoryName());
        category.setDescription(dto.getDescription());
        category.setIcon(dto.getIcon());
        category.setCategoryStatus(dto.getCategoryStatus() != null ? dto.getCategoryStatus() : CategoryStatus.ACTIVE);
        category.setDisplayOrder(dto.getDisplayOrder());
        return category;
    }
}
