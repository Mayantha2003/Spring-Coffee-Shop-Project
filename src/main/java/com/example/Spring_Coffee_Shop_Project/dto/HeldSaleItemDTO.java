package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HeldSaleItemDTO {

    private long heldSaleItemId;
    private long itemId;
    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private String notes;
}