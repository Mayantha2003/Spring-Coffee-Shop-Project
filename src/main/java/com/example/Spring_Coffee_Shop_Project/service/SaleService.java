package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.dto.*;
import com.example.Spring_Coffee_Shop_Project.enumeration.SaleStatus;

import java.util.List;

public interface SaleService {

    SaleDTO createSale(SaleCreateDTO saleCreateDTO);

    SaleDTO getSaleById(long id);

    List<SaleDTO> getAllSales();

    List<SaleDTO> getSalesByBatch(long batchId);

    List<SaleDTO> getSalesByCustomer(long customerId);

    List<SaleDTO> getSalesByStatus(SaleStatus status);

    SaleDTO cancelSale(long id);

    SalesHistoryResponseDTO getSalesHistory(SalesHistoryFilterDTO filter);

    List<ItemSalesChartDTO> getItemSalesChart(String period, String category, Long batchId);
}