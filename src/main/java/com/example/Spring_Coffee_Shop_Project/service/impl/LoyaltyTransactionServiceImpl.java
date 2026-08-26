package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.entity.LoyaltyTransaction;
import com.example.Spring_Coffee_Shop_Project.repository.LoyaltyTransactionRepository;
import com.example.Spring_Coffee_Shop_Project.service.LoyaltyTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoyaltyTransactionServiceImpl implements LoyaltyTransactionService {

    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Override
    public LoyaltyTransaction saveTransaction(LoyaltyTransaction transaction) {

        return loyaltyTransactionRepository.save(transaction);
    }

    @Override
    public List<LoyaltyTransaction> getAllTransactions() {

        return loyaltyTransactionRepository.findAll();
    }

    @Override
    public List<LoyaltyTransaction> getTransactionsByCustomer(Long customerId) {

        return loyaltyTransactionRepository.findByCustomerCustomerId(customerId);
    }

//    @Override
//    public List<LoyaltyTransaction> getTransactionsBySale(Long saleId) {
//
//        return loyaltyTransactionRepository.findBySaleSaleId(saleId);
//    }
}