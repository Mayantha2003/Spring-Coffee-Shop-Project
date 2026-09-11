package com.example.Spring_Coffee_Shop_Project.controller;

import com.example.Spring_Coffee_Shop_Project.constant.CommonResponse;
import com.example.Spring_Coffee_Shop_Project.dto.InvoiceConfirmDTO;
import com.example.Spring_Coffee_Shop_Project.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping(value = "v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping(value = "/scan", consumes = "multipart/form-data")
    public ResponseEntity<CommonResponse> scanInvoice(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(new CommonResponse(200, invoiceService.scanInvoice(file), "Invoice scanned successfully"));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<CommonResponse> confirmInvoice(@PathVariable("id") long invoiceId,
                                                         @RequestBody InvoiceConfirmDTO confirmDTO) {
        return ResponseEntity.ok(new CommonResponse(200, invoiceService.confirmInvoice(invoiceId, confirmDTO), "Invoice confirmed and stock updated"));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<CommonResponse> rejectInvoice(@PathVariable("id") long invoiceId) {
        return ResponseEntity.ok(new CommonResponse(200, invoiceService.rejectInvoice(invoiceId), "Invoice rejected"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse> getInvoice(@PathVariable("id") long invoiceId) {
        return ResponseEntity.ok(new CommonResponse(200, invoiceService.getInvoice(invoiceId), "Invoice fetched successfully"));
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getAllInvoices() {
        return ResponseEntity.ok(new CommonResponse(200, invoiceService.getAllInvoices(), "Invoices fetched successfully"));
    }
}