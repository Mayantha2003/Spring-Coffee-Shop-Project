package com.example.Spring_Coffee_Shop_Project.entity;

import com.example.Spring_Coffee_Shop_Project.enumeration.UserRole;
import com.example.Spring_Coffee_Shop_Project.enumeration.UserStatus;
import jakarta.persistence.*;
import lombok.*;

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
    private String  phone;

    @Enumerated(EnumType.STRING)
    private UserStatus userstatus;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private boolean isVerified = false;
    private String verificationToken;
}