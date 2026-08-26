package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.BatchCloseDTO;
import com.example.Spring_Coffee_Shop_Project.dto.BatchDTO;
import com.example.Spring_Coffee_Shop_Project.dto.BatchOpenDTO;
import com.example.Spring_Coffee_Shop_Project.entity.Batch;
import com.example.Spring_Coffee_Shop_Project.enumeration.BatchStatus;
import com.example.Spring_Coffee_Shop_Project.exception.CustomerException;
import com.example.Spring_Coffee_Shop_Project.repository.BatchRepository;
import com.example.Spring_Coffee_Shop_Project.service.BatchService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;

    @Override
    @Transactional
    public BatchDTO openBatch(BatchOpenDTO batchOpenDTO) {

        log.info("Executing Open Batch for User: {}", batchOpenDTO.getOpenedBy());

        Optional<Batch> openBatch = batchRepository.findByBatchStatus(BatchStatus.OPEN);

        if (openBatch.isPresent()) {
            throw new CustomerException(400, "An active batch (" + openBatch.get().getBatchCode() + ") is already open!");
        }

        // Auto-generate Unique Batch Code (e.g., BATCH-20260818-1030)
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String generatedCode = "BATCH-" + datePrefix;

        Batch batch = new Batch();
        batch.setBatchCode(generatedCode);
        batch.setOpenedBy(batchOpenDTO.getOpenedBy());
        batch.setOpenedAt(LocalDateTime.now());
        batch.setStartingCash(batchOpenDTO.getStartingCash());
        batch.setTotalSales(BigDecimal.ZERO);
        batch.setTotalProfit(BigDecimal.ZERO);
        batch.setTotalOrders(0);
        batch.setBatchStatus(BatchStatus.OPEN);

        Batch savedBatch = batchRepository.save(batch);

        return mapToDTO(savedBatch);
    }

    @Override
    @Transactional
    public BatchDTO closeBatch(BatchCloseDTO batchCloseDTO) {

        log.info("Executing Close Batch by User: {}", batchCloseDTO.getClosedBy());

        Optional<Batch> activeBatch = batchRepository.findByBatchStatus(BatchStatus.OPEN);

        if (activeBatch.isEmpty()) {
            throw new CustomerException(404, "No active batch found to close!");
        }

        Batch batch = activeBatch.get();
        batch.setClosedBy(batchCloseDTO.getClosedBy());
        batch.setClosedAt(LocalDateTime.now());
        batch.setEndingCash(batchCloseDTO.getEndingCash());
        batch.setBatchStatus(BatchStatus.CLOSED);

        Batch savedBatch = batchRepository.save(batch);

        return mapToDTO(savedBatch);
    }

    @Override
    public BatchDTO getActiveBatch() {

        log.info("Fetching Active Batch...");

        Optional<Batch> activeBatch = batchRepository.findByBatchStatus(BatchStatus.OPEN);

        if (activeBatch.isEmpty()) {
            throw new CustomerException(404, "No active batch currently open.");
        }

        return mapToDTO(activeBatch.get());
    }

    @Override
    public List<BatchDTO> getAllBatches() {

        log.info("Fetching all batches...");

        List<Batch> batches = batchRepository.findAllByOrderByOpenedAtDesc();
        List<BatchDTO> batchDTOList = new ArrayList<>();

        for (Batch batch : batches) {
            batchDTOList.add(mapToDTO(batch));
        }

        return batchDTOList;
    }

    private BatchDTO mapToDTO(Batch batch) {
        return BatchDTO.builder()
                .batchId(batch.getBatchId())
                .batchCode(batch.getBatchCode())
                .openedBy(batch.getOpenedBy())
                .openedAt(batch.getOpenedAt())
                .closedBy(batch.getClosedBy())
                .closedAt(batch.getClosedAt())
                .startingCash(batch.getStartingCash())
                .endingCash(batch.getEndingCash())
                .totalSales(batch.getTotalSales())
                .totalProfit(batch.getTotalProfit())
                .totalOrders(batch.getTotalOrders())
                .batchStatus(batch.getBatchStatus())
                .build();
    }
}