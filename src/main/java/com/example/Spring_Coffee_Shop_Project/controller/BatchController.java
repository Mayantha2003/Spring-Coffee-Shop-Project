package com.example.Spring_Coffee_Shop_Project.controller;

import com.example.Spring_Coffee_Shop_Project.dto.BatchCloseDTO;
import com.example.Spring_Coffee_Shop_Project.dto.BatchDTO;
import com.example.Spring_Coffee_Shop_Project.dto.BatchOpenDTO;
import com.example.Spring_Coffee_Shop_Project.enumeration.BatchStatus;
import com.example.Spring_Coffee_Shop_Project.enumeration.UserStatus;
import com.example.Spring_Coffee_Shop_Project.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("v1/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @PostMapping("/open")
    public ResponseEntity<BatchDTO> openBatch(@RequestBody BatchOpenDTO batchOpenDTO) {
        return ResponseEntity.ok(batchService.openBatch(batchOpenDTO));
    }

    @PostMapping("/close")
    public ResponseEntity<BatchDTO> closeBatch(@RequestBody BatchCloseDTO batchCloseDTO) {
        return ResponseEntity.ok(batchService.closeBatch(batchCloseDTO));
    }

    @GetMapping("/active")
    public ResponseEntity<BatchDTO> getActiveBatch() {
        return ResponseEntity.ok(batchService.getActiveBatch());
    }

    @GetMapping
    public ResponseEntity<List<BatchDTO>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/statuses")
    public ResponseEntity<BatchStatus[]> getBatchStatuses() {
        return ResponseEntity.ok(BatchStatus.values());
    }
}