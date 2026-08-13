package com.example.Spring_Coffee_Shop_Project.service;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String token);
}
