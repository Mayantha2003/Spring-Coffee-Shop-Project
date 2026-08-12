package com.example.Spring_Coffee_Shop_Project.dto;

import com.example.Spring_Coffee_Shop_Project.enumeration.UserRole;
import com.example.Spring_Coffee_Shop_Project.enumeration.UserStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private long userId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private UserStatus userstatus;
    private UserRole userRole;
}