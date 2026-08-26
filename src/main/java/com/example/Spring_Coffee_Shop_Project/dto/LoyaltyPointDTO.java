package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoyaltyPointDTO {

    private long loyaltyPointId;
    private int availablePoints;
    private int totalEarned;
    private int totalRedeemed;
}