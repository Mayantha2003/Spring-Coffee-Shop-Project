package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhoneAndCustomerIdNot(String phone, long customerId);

    boolean existsByEmailAndCustomerIdNot(String email, long customerId);

    Optional<Customer> findByPhone(String phone);

    @Query("""
        SELECT DISTINCT c
        FROM Customer c
        LEFT JOIN FETCH c.loyaltyPoint
        """)
    List<Customer> findAllWithLoyaltyPoint();

    @Query("""
        SELECT DISTINCT c
        FROM Customer c
        LEFT JOIN FETCH c.loyaltyPoint
        WHERE LOWER(c.firstName) LIKE LOWER(CONCAT(:prefix, '%'))
           OR LOWER(c.lastName) LIKE LOWER(CONCAT(:prefix, '%'))
        """)
    List<Customer> searchByNameWithLoyaltyPoint(String prefix);

    @Query("""
        SELECT DISTINCT c
        FROM Customer c
        LEFT JOIN FETCH c.loyaltyPoint
        WHERE c.phone LIKE CONCAT('%', :phone, '%')
        """)
    List<Customer> searchByPhoneWithLoyaltyPoint(String phone);
}