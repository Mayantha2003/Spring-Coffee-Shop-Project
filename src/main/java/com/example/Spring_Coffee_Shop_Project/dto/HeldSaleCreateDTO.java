package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HeldSaleCreateDTO {

    private Long customerId;
    private Long batchId;
    private String notes;
    private List<HeldSaleItemDTO> items;
}