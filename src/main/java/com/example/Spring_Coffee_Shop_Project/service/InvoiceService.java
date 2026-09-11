package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.dto.InvoiceConfirmDTO;
import com.example.Spring_Coffee_Shop_Project.dto.SupplierInvoiceDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InvoiceService {

    SupplierInvoiceDTO scanInvoice(MultipartFile file);

    SupplierInvoiceDTO confirmInvoice(long invoiceId, InvoiceConfirmDTO confirmDTO);

    SupplierInvoiceDTO rejectInvoice(long invoiceId);

    SupplierInvoiceDTO getInvoice(long invoiceId);

    List<SupplierInvoiceDTO> getAllInvoices();
}