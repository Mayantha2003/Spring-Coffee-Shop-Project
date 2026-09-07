package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.entity.LoyaltyPoint;
import com.example.Spring_Coffee_Shop_Project.repository.LoyaltyPointRepository;
import com.example.Spring_Coffee_Shop_Project.service.LoyaltyPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoyaltyPointServiceImpl implements LoyaltyPointService {

    private final LoyaltyPointRepository loyaltyPointRepository;

    @Override
    public LoyaltyPoint saveLoyaltyPoint(LoyaltyPoint loyaltyPoint) {
        return loyaltyPointRepository.save(loyaltyPoint);
    }

    @Override
    public LoyaltyPoint getByCustomerId(long customerId) {
        return loyaltyPointRepository.findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Loyalty points not found for customer: " + customerId ));
    }

    @Override
    public LoyaltyPoint updatePoints(long customerId, int points) {

        LoyaltyPoint loyaltyPoint = getByCustomerId(customerId);

        loyaltyPoint.setAvailablePoints(loyaltyPoint.getAvailablePoints() + points
        );

        if (points > 0) {
            loyaltyPoint.setTotalEarned(loyaltyPoint.getTotalEarned() + points);
        }

        return loyaltyPointRepository.save(loyaltyPoint);
    }
}