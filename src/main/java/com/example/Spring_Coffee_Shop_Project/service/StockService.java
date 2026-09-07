package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.dto.StockAdjustDTO;
import com.example.Spring_Coffee_Shop_Project.dto.StockDTO;
import com.example.Spring_Coffee_Shop_Project.dto.StockTransactionDTO;
import com.example.Spring_Coffee_Shop_Project.service.impl.StockServiceImpl.StockStatistics;

import java.util.List;

public interface StockService {

    List<StockDTO> getAllStocks();

    StockDTO getStockById(Long id);

    StockDTO getStockByItemId(Long itemId);

    List<StockDTO> getLowStockItems();

    List<StockDTO> getOutOfStockItems();

    StockStatistics getStockStatistics();

    StockTransactionDTO adjustStock(StockAdjustDTO adjustDTO);

    StockDTO initializeStock(Long itemId, int initialQuantity, int reorderLevel);

    StockDTO updateReorderLevel(Long stockId, int newReorderLevel);

    StockDTO updateMaxStockLevel(Long stockId, int maxStockLevel);

    List<StockTransactionDTO> getAllTransactions();
}