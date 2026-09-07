package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Customer> findByFirstNameStartingWithIgnoreCaseOrLastNameStartingWithIgnoreCase(String firstNamePrefix, String lastNamePrefix);

    List<Customer> findByPhoneContaining(String phone);
}