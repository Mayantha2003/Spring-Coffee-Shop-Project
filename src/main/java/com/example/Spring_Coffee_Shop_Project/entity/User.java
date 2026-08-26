package com.example.Spring_Coffee_Shop_Project.entity;

import com.example.Spring_Coffee_Shop_Project.enumeration.UserRole;
import com.example.Spring_Coffee_Shop_Project.enumeration.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;
    private String phone;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private boolean isVerified = false;
    private String verificationToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoginHistory> loginHistories = new ArrayList<>();

    @OneToMany(mappedBy = "performedBy", fetch = FetchType.LAZY)
    private List<StockTransaction> stockTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "heldBy", fetch = FetchType.LAZY)
    private List<HeldSale> heldSales = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Sale> sales = new ArrayList<>();
}