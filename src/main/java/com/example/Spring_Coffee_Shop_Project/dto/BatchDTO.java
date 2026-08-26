package com.example.Spring_Coffee_Shop_Project.dto;

import com.example.Spring_Coffee_Shop_Project.enumeration.BatchStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchDTO {

    private long batchId;
    private String batchCode;

    private String openedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime openedAt;

    private String closedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime closedAt;

    private BigDecimal startingCash;
    private BigDecimal endingCash;

    private BigDecimal totalSales;
    private BigDecimal totalProfit;
    private int totalOrders;

    private BatchStatus batchStatus;
}