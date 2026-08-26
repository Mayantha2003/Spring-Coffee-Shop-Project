package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockDTO {

    private long stockId;
    private long itemId;
    private String itemName;
    private int availableQuantity;
    private int reorderLevel;
    private Integer maxStockLevel;
}