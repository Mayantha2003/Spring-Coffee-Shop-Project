package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCreateDTO {

    private BigDecimal totalPaid;
    private BigDecimal changeAmount;
    private List<PaymentDetailDTO> paymentDetails;
}