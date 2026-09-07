package com.example.Spring_Coffee_Shop_Project.controller;

import com.example.Spring_Coffee_Shop_Project.dto.*;
import com.example.Spring_Coffee_Shop_Project.enumeration.SaleStatus;
import com.example.Spring_Coffee_Shop_Project.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleDTO> createSale(@RequestBody SaleCreateDTO saleCreateDTO) {
        return ResponseEntity.ok(saleService.createSale(saleCreateDTO));
    }

    @GetMapping("/history")
    public ResponseEntity<SalesHistoryResponseDTO> getSalesHistory(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false, defaultValue = "customer") String groupBy
    ) {
        SalesHistoryFilterDTO filter = new SalesHistoryFilterDTO();
        filter.setBatchId(batchId);
        filter.setYear(year);
        filter.setMonth(month);
        filter.setDay(day);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setSort(sort);
        filter.setGroupBy(groupBy);

        return ResponseEntity.ok(saleService.getSalesHistory(filter));
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<SaleDTO>> getSalesByBatch(@PathVariable long batchId) {
        return ResponseEntity.ok(saleService.getSalesByBatch(batchId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SaleDTO>> getSalesByCustomer(@PathVariable long customerId) {
        return ResponseEntity.ok(saleService.getSalesByCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SaleDTO>> getSalesByStatus(@PathVariable SaleStatus status) {
        return ResponseEntity.ok(saleService.getSalesByStatus(status));
    }

    @GetMapping("/statuses")
    public ResponseEntity<SaleStatus[]> getSaleStatuses() {
        return ResponseEntity.ok(SaleStatus.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleDTO> getSaleById(@PathVariable long id) {
        return ResponseEntity.ok(saleService.getSaleById(id));
    }

    @GetMapping
    public ResponseEntity<List<SaleDTO>> getAllSales() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<SaleDTO> cancelSale(@PathVariable long id) {
        return ResponseEntity.ok(saleService.cancelSale(id));
    }
}