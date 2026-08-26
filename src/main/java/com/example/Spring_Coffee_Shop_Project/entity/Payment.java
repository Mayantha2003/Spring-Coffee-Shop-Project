package com.example.Spring_Coffee_Shop_Project.entity;

import com.example.Spring_Coffee_Shop_Project.enumeration.PaymentStatus;
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
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false, unique = true)
    private Sale sale;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPaid;

    @Column(precision = 12, scale = 2)
    private BigDecimal changeAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentDetail> paymentDetails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        paidAt = LocalDateTime.now();
    }
}