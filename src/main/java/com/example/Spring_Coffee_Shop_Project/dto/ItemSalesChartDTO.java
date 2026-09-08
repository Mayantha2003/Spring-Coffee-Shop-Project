package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemSalesChartDTO {

    private String name;
    private String category;
    private int units;
    private BigDecimal revenue;
    private BigDecimal profit;
}