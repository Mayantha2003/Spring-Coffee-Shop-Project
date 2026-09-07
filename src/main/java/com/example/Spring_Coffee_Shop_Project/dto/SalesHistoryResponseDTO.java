package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesHistoryResponseDTO {

    private SalesHistorySummaryDTO summary;
    private List<TopItemDTO> topItems;
    private List<SalesHistoryItemDTO> sales;
}