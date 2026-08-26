package com.example.Spring_Coffee_Shop_Project.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LoyaltyPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long loyaltyPointId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            unique = true
    )
    private Customer customer;

    @Column(nullable = false)
    private int availablePoints = 0;

    @Column(nullable = false)
    private int totalEarned = 0;

    @Column(nullable = false)
    private int totalRedeemed = 0;
}