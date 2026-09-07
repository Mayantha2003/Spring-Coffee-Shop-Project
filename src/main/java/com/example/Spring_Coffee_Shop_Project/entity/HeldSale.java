package com.example.Spring_Coffee_Shop_Project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class HeldSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long heldSaleId;

    @Column(nullable = false, unique = true)
    private String holdCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User heldBy;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime heldAt;

    @Column(nullable = false)
    private boolean removalRequested = false;

    @OneToMany(mappedBy = "heldSale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HeldSaleItem> heldSaleItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        heldAt = LocalDateTime.now();
    }
}