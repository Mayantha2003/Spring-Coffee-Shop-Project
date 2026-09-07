package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleCreateDTO {

    private Long customerId;
    private Long batchId;
    private BigDecimal discountAmount;
    private String notes;
    private List<SaleItemDTO> items;
    private PaymentCreateDTO payment;
    private Integer loyaltyPointsUsed;
}