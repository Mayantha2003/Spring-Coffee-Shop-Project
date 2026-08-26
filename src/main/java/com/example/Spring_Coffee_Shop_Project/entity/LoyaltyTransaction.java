package com.example.Spring_Coffee_Shop_Project.entity;

import com.example.Spring_Coffee_Shop_Project.enumeration.LoyaltyTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long loyaltyTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoyaltyTransactionType transactionType;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    private String description;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
    }
}