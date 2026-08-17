package com.example.Spring_Coffee_Shop_Project.entity;

import com.example.Spring_Coffee_Shop_Project.enumeration.ItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
