package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.UserDTO;
import com.example.Spring_Coffee_Shop_Project.entity.LoginHistory;
import com.example.Spring_Coffee_Shop_Project.entity.User;
import com.example.Spring_Coffee_Shop_Project.repository.LoginHistoryRepository;
import com.example.Spring_Coffee_Shop_Project.service.LoginHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    @Override
    public void recordLogin(UserDTO userDTO, HttpServletRequest request) {
        User user = new User();
        user.setUserId(userDTO.getUserId());

        LoginHistory loginHistory = LoginHistory.builder()
                .user(user)
                .loginTime(LocalDateTime.now())
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();

        loginHistoryRepository.save(loginHistory);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}