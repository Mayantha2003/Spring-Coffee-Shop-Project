package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.SupplierInvoice;
import com.example.Spring_Coffee_Shop_Project.enumeration.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, Long> {

    List<SupplierInvoice> findAllByOrderByCreatedAtDesc();

    @Query("SELECT i FROM SupplierInvoice i LEFT JOIN FETCH i.items WHERE i.invoiceId = :id")
    SupplierInvoice findByIdWithItems(long id);
}