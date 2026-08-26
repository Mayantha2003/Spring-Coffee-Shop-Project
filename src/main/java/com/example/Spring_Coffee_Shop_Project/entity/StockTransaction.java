package com.example.Spring_Coffee_Shop_Project.entity;

import com.example.Spring_Coffee_Shop_Project.enumeration.StockTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long stockTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockTransactionType transactionType;

    @Column(nullable = false)
    private int quantity;

    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User performedBy;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
    }
}