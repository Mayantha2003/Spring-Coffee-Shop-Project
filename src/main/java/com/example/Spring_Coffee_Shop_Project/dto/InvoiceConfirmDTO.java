package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceConfirmDTO {

    private String supplierName;
    private LocalDate invoiceDate;
    private Long performedByUserId;
    private List<InvoiceConfirmItemDTO> items;
}