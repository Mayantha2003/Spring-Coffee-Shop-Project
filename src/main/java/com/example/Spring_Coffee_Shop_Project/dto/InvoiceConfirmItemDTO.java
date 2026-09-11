package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceConfirmItemDTO {

    private long invoiceItemId;
    private Long matchedItemId;
    private int quantity;
    private BigDecimal unitPrice;
}