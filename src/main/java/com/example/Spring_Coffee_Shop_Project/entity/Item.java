package com.example.Spring_Coffee_Shop_Project.entity;

import com.example.Spring_Coffee_Shop_Project.enumeration.ItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long itemId;

    @Column(nullable = false)
    private String itemName;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal discountPrice;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    private boolean isVeg = true;
    private int prepTimeMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Stock stock;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<StockTransaction> stockTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<SaleItem> saleItems = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<HeldSaleItem> heldSaleItems = new ArrayList<>();
}
