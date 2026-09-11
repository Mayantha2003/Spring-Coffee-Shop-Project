package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupplierInvoiceItemDTO {

    private long invoiceItemId;
    private String extractedItemName;
    private int quantity;
    private BigDecimal unitPrice;

    private Long matchedItemId;
    private String matchedItemName;
    private Double matchConfidence;
}