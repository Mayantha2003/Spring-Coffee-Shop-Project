package com.example.Spring_Coffee_Shop_Project.dto;

import com.example.Spring_Coffee_Shop_Project.enumeration.ItemStatus;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDTO {

    private long itemId;
    private String itemName;
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal discountPrice;
    private String imageUrl;
    private ItemStatus itemStatus;
    private boolean isVeg;
    private int prepTimeMinutes;
    private Integer points;
    private long categoryId;
}