package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.dto.UserDTO;
import com.example.Spring_Coffee_Shop_Project.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginHistoryService {

    void recordLogin(UserDTO userDTO, HttpServletRequest request);
}