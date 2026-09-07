package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.dto.HeldSaleCreateDTO;
import com.example.Spring_Coffee_Shop_Project.dto.HeldSaleDTO;

import java.util.List;

public interface HeldSaleService {

    HeldSaleDTO createHeldSale(HeldSaleCreateDTO dto);

    HeldSaleDTO getHeldSaleById(long id);

    List<HeldSaleDTO> getAllHeldSales();

    List<HeldSaleDTO> getHeldSalesByBatch(long batchId);

    HeldSaleDTO requestRemoval(long id);

    void deleteHeldSale(long id);
}