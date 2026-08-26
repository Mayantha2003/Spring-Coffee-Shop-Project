package com.example.Spring_Coffee_Shop_Project.entity;

import com.example.Spring_Coffee_Shop_Project.enumeration.BatchStatus;
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
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long batchId;

    @Column(nullable = false, unique = true, updatable = false)
    private String batchCode;

    @Column(nullable = false, updatable = false)
    private String openedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime openedAt;

    private String closedBy;
    private LocalDateTime closedAt;

    @Column(nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal startingCash;

    @Column(precision = 12, scale = 2)
    private BigDecimal endingCash;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProfit = BigDecimal.ZERO;

    @Column(nullable = false)
    private int totalOrders = 0;

    @Enumerated(EnumType.STRING)
    private BatchStatus batchStatus;

    @OneToMany(mappedBy = "batch")
    private List<Sale> sales = new ArrayList<>();

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HeldSale> heldSales = new ArrayList<>();

    public void recordSale(BigDecimal saleAmount, BigDecimal profitAmount) {
        this.totalSales = (this.totalSales == null ? BigDecimal.ZERO : this.totalSales).add(saleAmount);
        this.totalProfit = (this.totalProfit == null ? BigDecimal.ZERO : this.totalProfit).add(profitAmount);
        this.totalOrders = this.totalOrders + 1;
    }
}