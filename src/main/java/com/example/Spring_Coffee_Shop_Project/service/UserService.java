package com.example.Spring_Coffee_Shop_Project.service;


import com.example.Spring_Coffee_Shop_Project.dto.UserDTO;

public interface UserService {

    UserDTO getUserDetails(String username, String password);

    void saveUser(UserDTO userDTO);
}
