package com.example.Spring_Coffee_Shop_Project.dto;

import com.example.Spring_Coffee_Shop_Project.enumeration.InvoiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupplierInvoiceDTO {

    private long invoiceId;
    private String supplierName;
    private LocalDate invoiceDate;
    private String imageUrl;
    private InvoiceStatus status;
    private BigDecimal totalAmount;
    private List<SupplierInvoiceItemDTO> items;
}