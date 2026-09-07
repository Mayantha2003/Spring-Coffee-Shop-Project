package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.StockTransaction;
import com.example.Spring_Coffee_Shop_Project.enumeration.StockTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findByItem_ItemIdOrderByTransactionDateDesc(Long itemId);

    List<StockTransaction> findByTransactionType(StockTransactionType type);

    List<StockTransaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT st FROM StockTransaction st JOIN FETCH st.item i JOIN FETCH st.performedBy u ORDER BY st.transactionDate DESC")
    List<StockTransaction> findAllWithDetails();

    @Query("SELECT st FROM StockTransaction st JOIN FETCH st.item i WHERE i.itemId = :itemId ORDER BY st.transactionDate DESC")
    List<StockTransaction> findByItemIdWithDetails(@Param("itemId") Long itemId);

    @Query("SELECT COUNT(st) FROM StockTransaction st WHERE st.transactionType = :type")
    long countByTransactionType(@Param("type") StockTransactionType type);

    @Query("SELECT SUM(st.quantity) FROM StockTransaction st WHERE st.transactionType = 'IN' AND st.item.itemId = :itemId")
    Integer sumIncomingQuantityByItem(@Param("itemId") Long itemId);

    @Query("SELECT SUM(st.quantity) FROM StockTransaction st WHERE st.transactionType = 'OUT' AND st.item.itemId = :itemId")
    Integer sumOutgoingQuantityByItem(@Param("itemId") Long itemId);
}