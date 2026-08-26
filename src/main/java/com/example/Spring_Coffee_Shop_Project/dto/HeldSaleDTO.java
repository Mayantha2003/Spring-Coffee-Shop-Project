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
public class HeldSaleDTO {

    private long heldSaleId;
    private String holdCode;
    private long batchId;
    private Long customerId;
    private String customerName;
    private long heldByUserId;
    private String heldByName;
    private BigDecimal subTotal;
    private BigDecimal totalAmount;
    private String notes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime heldAt;

    private List<HeldSaleItemDTO> heldSaleItems;
}