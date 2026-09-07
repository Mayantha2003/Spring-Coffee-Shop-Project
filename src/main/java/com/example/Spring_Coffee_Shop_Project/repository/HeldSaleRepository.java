package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.HeldSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeldSaleRepository extends JpaRepository<HeldSale, Long> {

    List<HeldSale> findAllByOrderByHeldAtDesc();

    List<HeldSale> findByBatchBatchId(Long batchId);

    List<HeldSale> findByCustomerCustomerId(Long customerId);
}