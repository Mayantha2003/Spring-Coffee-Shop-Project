package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.entity.LoyaltyTransaction;

import java.util.List;

public interface LoyaltyTransactionService {

    LoyaltyTransaction saveTransaction(LoyaltyTransaction transaction);

    List<LoyaltyTransaction> getAllTransactions();

    List<LoyaltyTransaction> getTransactionsByCustomer(Long customerId);

    //List<LoyaltyTransaction> getTransactionsBySale(Long saleId);
}