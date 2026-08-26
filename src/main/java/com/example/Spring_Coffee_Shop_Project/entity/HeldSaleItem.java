package com.example.Spring_Coffee_Shop_Project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class HeldSaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long heldSaleItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_sale_id", nullable = false)
    private HeldSale heldSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    private String notes;
}