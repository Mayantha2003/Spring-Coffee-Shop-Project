package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.Sale;
import com.example.Spring_Coffee_Shop_Project.enumeration.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByBatchBatchId(long batchId);
    List<Sale> findByCustomerCustomerId(long customerId);
    List<Sale> findByUserUserId(long userId);
    List<Sale> findBySaleStatus(SaleStatus saleStatus);
    List<Sale> findAllByOrderByCreatedAtDesc();
    Optional<Sale> findBySaleCode(String saleCode);
    boolean existsBySaleCode(String saleCode);
    long countByBatchBatchId(long batchId);

    @Query("SELECT s FROM Sale s JOIN FETCH s.saleItems si JOIN FETCH si.item WHERE s.saleId = :saleId")
    Optional<Sale> findByIdWithItems(@Param("saleId") long saleId);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ===== Sales History filter query =====
    @Query("""
        SELECT DISTINCT s FROM Sale s
        LEFT JOIN FETCH s.customer
        LEFT JOIN FETCH s.batch
        LEFT JOIN FETCH s.user
        LEFT JOIN FETCH s.saleItems si
        LEFT JOIN FETCH si.item
        WHERE s.saleStatus = com.example.Spring_Coffee_Shop_Project.enumeration.SaleStatus.COMPLETED
          AND (:batchId IS NULL OR s.batch.batchId = :batchId)
          AND (:year IS NULL OR YEAR(s.createdAt) = :year)
          AND (:month IS NULL OR MONTH(s.createdAt) = :month)
          AND (:day IS NULL OR DAY(s.createdAt) = :day)
          AND (:fromDate IS NULL OR s.createdAt >= :fromDate)
          AND (:toDate IS NULL OR s.createdAt < :toDate)
        ORDER BY s.createdAt DESC
        """)
    List<Sale> findSalesHistory(
            @Param("batchId") Long batchId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("day") Integer day,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}