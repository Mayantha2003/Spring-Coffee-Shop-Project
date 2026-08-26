package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchOpenDTO {

    private String openedBy;
    private BigDecimal startingCash;
}
