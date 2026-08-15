package com.example.Spring_Coffee_Shop_Project.entity;

import com.example.Spring_Coffee_Shop_Project.enumeration.CategoryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long categoryId;

    @Column(nullable = false, unique = true)
    private String categoryName;

    private String description;
    private String icon;

    @Enumerated(EnumType.STRING)
    private CategoryStatus categoryStatus;

    private int displayOrder = 0;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<Item> items;
}