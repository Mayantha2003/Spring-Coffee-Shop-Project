package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.Category;
import com.example.Spring_Coffee_Shop_Project.enumeration.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCategoryName(String categoryName);

    Optional<Category> findByCategoryName(String categoryName);

    List<Category> findByCategoryStatus(CategoryStatus status);
}