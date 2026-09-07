package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopItemDTO {

    private String itemName;
    private int quantity;
    private BigDecimal revenue;
}