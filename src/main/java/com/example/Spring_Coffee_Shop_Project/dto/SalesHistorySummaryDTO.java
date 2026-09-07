package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesHistorySummaryDTO {

    private long totalSales;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private BigDecimal profitPercentage;
    private long totalCustomers;
    private long flaggedCount;
}