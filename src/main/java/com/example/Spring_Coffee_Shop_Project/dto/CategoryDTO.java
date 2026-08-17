package com.example.Spring_Coffee_Shop_Project.dto;

import com.example.Spring_Coffee_Shop_Project.enumeration.CategoryStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO{

    private long categoryId;
    private String categoryName;
    private String description;
    private String icon;
    private CategoryStatus categoryStatus;
    private int displayOrder;
    private int itemCount;

}