package com.example.Spring_Coffee_Shop_Project.dto;

import com.example.Spring_Coffee_Shop_Project.enumeration.CustomerStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDTO {

    private long customerId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private CustomerStatus customerStatus;
    private boolean verified;
    private LocalDateTime registeredAt;
    private LoyaltyPointDTO loyaltyPoint;
}