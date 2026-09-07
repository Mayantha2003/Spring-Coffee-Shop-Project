package com.example.Spring_Coffee_Shop_Project.controller;

import com.example.Spring_Coffee_Shop_Project.dto.HeldSaleCreateDTO;
import com.example.Spring_Coffee_Shop_Project.dto.HeldSaleDTO;
import com.example.Spring_Coffee_Shop_Project.service.HeldSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/v1/held-sales")
@RequiredArgsConstructor
public class HeldSaleController {

    private final HeldSaleService heldSaleService;

    @PostMapping
    public ResponseEntity<HeldSaleDTO> create(@RequestBody HeldSaleCreateDTO dto) {
        return ResponseEntity.ok(heldSaleService.createHeldSale(dto));
    }

    @GetMapping
    public ResponseEntity<List<HeldSaleDTO>> getAll() {
        return ResponseEntity.ok(heldSaleService.getAllHeldSales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeldSaleDTO> getById(@PathVariable long id) {
        return ResponseEntity.ok(heldSaleService.getHeldSaleById(id));
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<HeldSaleDTO>> getByBatch(@PathVariable long batchId) {
        return ResponseEntity.ok(heldSaleService.getHeldSalesByBatch(batchId));
    }

    @PutMapping("/{id}/request-removal")
    public ResponseEntity<HeldSaleDTO> requestRemoval(@PathVariable long id) {
        return ResponseEntity.ok(heldSaleService.requestRemoval(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable long id) {
        heldSaleService.deleteHeldSale(id);
        return ResponseEntity.ok(Map.of("message", "Held sale deleted successfully"));
    }
}