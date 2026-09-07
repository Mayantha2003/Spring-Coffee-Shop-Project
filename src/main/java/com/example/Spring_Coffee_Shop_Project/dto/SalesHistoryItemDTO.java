package com.example.Spring_Coffee_Shop_Project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesHistoryItemDTO {

    private long saleId;
    private String saleCode;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private long batchId;
    private String batchCode;
    private String itemSummary;
    private int totalQty;
    private BigDecimal totalAmount;
    private BigDecimal profit;
    private int pointsEarned;
    private boolean flagged;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private List<SaleItemDTO> saleItems;
}