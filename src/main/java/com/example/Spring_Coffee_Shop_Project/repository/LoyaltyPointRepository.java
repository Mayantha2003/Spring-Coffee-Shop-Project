package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.LoyaltyPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPoint, Long> {

    Optional<LoyaltyPoint> findByCustomerCustomerId(long customerId);
}