package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findBySaleSaleId(long saleId);

    List<SaleItem> findByItemItemId(long itemId);

    @Query("SELECT SUM(si.quantity) FROM SaleItem si WHERE si.item.itemId = :itemId")
    Integer sumQuantitySoldByItem(@Param("itemId") long itemId);

    @Query("SELECT si.item.itemId, SUM(si.quantity) FROM SaleItem si GROUP BY si.item.itemId ORDER BY SUM(si.quantity) DESC")
    List<Object[]> findTopSellingItems();
}