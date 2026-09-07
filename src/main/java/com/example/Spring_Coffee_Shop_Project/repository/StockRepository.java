package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByItem_ItemId(Long itemId);

    @Query("SELECT s FROM Stock s WHERE s.availableQuantity <= s.reorderLevel AND s.availableQuantity > 0")
    List<Stock> findLowStockItems();

    @Query("SELECT s FROM Stock s WHERE s.availableQuantity = 0")
    List<Stock> findOutOfStockItems();

    @Query("SELECT COUNT(s) FROM Stock s")
    long countTotalStockRecords();

    @Query("SELECT SUM(s.availableQuantity) FROM Stock s")
    Integer sumTotalAvailableQuantity();

    @Query("SELECT s FROM Stock s JOIN FETCH s.item i JOIN FETCH i.category")
    List<Stock> findAllWithItemAndCategory();

    @Query("SELECT s FROM Stock s JOIN FETCH s.item i WHERE i.itemId = :itemId")
    Optional<Stock> findByItemIdWithItem(@Param("itemId") Long itemId);

    boolean existsByItem_ItemId(Long itemId);
}