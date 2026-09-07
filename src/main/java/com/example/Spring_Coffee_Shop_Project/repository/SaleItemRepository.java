package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findBySaleSaleId(long saleId);

    List<SaleItem> findByItemItemId(long itemId);

    @Query("SELECT SUM(si.quantity) FROM SaleItem si WHERE si.item.itemId = :itemId")
    Integer sumQuantitySoldByItem(@Param("itemId") long itemId);

    @Query("SELECT si.item.itemId, SUM(si.quantity) FROM SaleItem si GROUP BY si.item.itemId ORDER BY SUM(si.quantity) DESC")
    List<Object[]> findTopSellingItems();

    // ========== ITEM SALES CHART ==========
    @Query("""
        SELECT 
            i.itemName,
            c.categoryName,
            SUM(si.quantity),
            SUM(si.lineTotal)
        FROM SaleItem si
        JOIN si.sale s
        JOIN si.item i
        JOIN i.category c
        WHERE s.saleStatus = com.example.Spring_Coffee_Shop_Project.enumeration.SaleStatus.COMPLETED
          AND (:fromDate IS NULL OR s.createdAt >= :fromDate)
          AND (:toDate   IS NULL OR s.createdAt <  :toDate)
          AND (:category IS NULL OR c.categoryName = :category)
          AND (:batchId  IS NULL OR s.batch.batchId = :batchId)
        GROUP BY i.itemId, i.itemName, c.categoryName
        ORDER BY SUM(si.quantity) DESC
        """)
    List<Object[]> findItemSalesChart(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate")   LocalDateTime toDate,
            @Param("category") String category,
            @Param("batchId")  Long batchId
    );
}