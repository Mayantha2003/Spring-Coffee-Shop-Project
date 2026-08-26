package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.entity.LoyaltyPoint;

public interface LoyaltyPointService {

    LoyaltyPoint saveLoyaltyPoint(LoyaltyPoint loyaltyPoint);

    LoyaltyPoint getByCustomerId(long customerId);

    LoyaltyPoint updatePoints(long customerId, int points);
}