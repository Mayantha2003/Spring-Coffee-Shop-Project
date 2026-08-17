package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.dto.UserDTO;
import com.example.Spring_Coffee_Shop_Project.entity.User;

import java.util.List;

public interface UserService {

    void saveUser(UserDTO userDTO);

    UserDTO getUserDetails(String username, String password);

    List<UserDTO> getAllUsers();

    UserDTO getUserByUsername(String username);

    void updateUser(UserDTO userDTO);

    void patchUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    boolean verifyUser(String token);

}
