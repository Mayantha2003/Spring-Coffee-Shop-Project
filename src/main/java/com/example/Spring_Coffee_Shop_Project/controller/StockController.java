package com.example.Spring_Coffee_Shop_Project.controller;

import com.example.Spring_Coffee_Shop_Project.dto.StockAdjustDTO;
import com.example.Spring_Coffee_Shop_Project.dto.StockDTO;
import com.example.Spring_Coffee_Shop_Project.dto.StockTransactionDTO;
import com.example.Spring_Coffee_Shop_Project.service.StockService;
import com.example.Spring_Coffee_Shop_Project.service.impl.StockServiceImpl.StockStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // Get all stocks
    // Stock UI
    @GetMapping
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        List<StockDTO> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }

    // Get stock by ID
    @GetMapping("/{id}")
    public ResponseEntity<StockDTO> getStockById(@PathVariable Long id) {
        StockDTO stock = stockService.getStockById(id);
        return ResponseEntity.ok(stock);
    }

    // Get stock by Item ID
    @GetMapping("/item/{itemId}")
    public ResponseEntity<StockDTO> getStockByItemId(@PathVariable Long itemId) {
        StockDTO stock = stockService.getStockByItemId(itemId);
        return ResponseEntity.ok(stock);
    }

    // Get low stock items
    @GetMapping("/low-stock")
    public ResponseEntity<List<StockDTO>> getLowStockItems() {
        List<StockDTO> stocks = stockService.getLowStockItems();
        return ResponseEntity.ok(stocks);
    }

    // Get out of stock items
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<StockDTO>> getOutOfStockItems() {
        List<StockDTO> stocks = stockService.getOutOfStockItems();
        return ResponseEntity.ok(stocks);
    }

    // Get stock statistics
    // Stock UI
    @GetMapping("/statistics")
    public ResponseEntity<StockStatistics> getStockStatistics() {
        StockStatistics statistics = stockService.getStockStatistics();
        return ResponseEntity.ok(statistics);
    }

    // Adjust stock (IN / OUT / ADJUSTMENT / SALE / WASTE)
    // Stock UI
    @PostMapping("/adjust")
    public ResponseEntity<StockTransactionDTO> adjustStock(@RequestBody StockAdjustDTO adjustDTO) {
        StockTransactionDTO transaction = stockService.adjustStock(adjustDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    // Initialize stock for an item
    @PostMapping("/initialize")
    public ResponseEntity<StockDTO> initializeStock(
            @RequestParam Long itemId,
            @RequestParam int initialQuantity,
            @RequestParam(defaultValue = "10") int reorderLevel) {

        StockDTO stock = stockService.initializeStock(itemId, initialQuantity, reorderLevel);
        return ResponseEntity.status(HttpStatus.CREATED).body(stock);
    }

    // Update reorder level
    @PutMapping("/{stockId}/reorder-level")
    public ResponseEntity<StockDTO> updateReorderLevel(
            @PathVariable Long stockId,
            @RequestParam int newReorderLevel) {

        StockDTO stock = stockService.updateReorderLevel(stockId, newReorderLevel);
        return ResponseEntity.ok(stock);
    }

    // Update max stock level
    @PutMapping("/{stockId}/max-level")
    public ResponseEntity<StockDTO> updateMaxStockLevel(
            @PathVariable Long stockId,
            @RequestParam int maxStockLevel) {

        StockDTO stock = stockService.updateMaxStockLevel(stockId, maxStockLevel);
        return ResponseEntity.ok(stock);
    }

    // Get all stock transactions
    // Stock UI
    @GetMapping("/transactions")
    public ResponseEntity<List<StockTransactionDTO>> getAllTransactions() {
        List<StockTransactionDTO> transactions = stockService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
}