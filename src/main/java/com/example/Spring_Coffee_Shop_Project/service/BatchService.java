package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.dto.BatchCloseDTO;
import com.example.Spring_Coffee_Shop_Project.dto.BatchDTO;
import com.example.Spring_Coffee_Shop_Project.dto.BatchOpenDTO;

import java.util.List;

public interface BatchService {

    BatchDTO openBatch(BatchOpenDTO batchOpenDTO);

    BatchDTO closeBatch(BatchCloseDTO batchCloseDTO);

    BatchDTO getActiveBatch();

    List<BatchDTO> getAllBatches();
}