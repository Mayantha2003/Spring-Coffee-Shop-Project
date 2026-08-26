package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.Batch;
import com.example.Spring_Coffee_Shop_Project.enumeration.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {

    Optional<Batch> findByBatchStatus(BatchStatus batchStatus);

    List<Batch> findAllByOrderByOpenedAtDesc();
}