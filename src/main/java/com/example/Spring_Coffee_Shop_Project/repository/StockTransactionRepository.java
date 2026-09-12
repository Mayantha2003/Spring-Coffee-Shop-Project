package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    @Query("SELECT st FROM StockTransaction st JOIN FETCH st.item i JOIN FETCH st.performedBy u ORDER BY st.transactionDate DESC")
    List<StockTransaction> findAllWithDetails();

}