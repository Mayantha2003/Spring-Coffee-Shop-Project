package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.HeldSaleCreateDTO;
import com.example.Spring_Coffee_Shop_Project.dto.HeldSaleDTO;
import com.example.Spring_Coffee_Shop_Project.dto.HeldSaleItemDTO;
import com.example.Spring_Coffee_Shop_Project.entity.*;
import com.example.Spring_Coffee_Shop_Project.enumeration.BatchStatus;
import com.example.Spring_Coffee_Shop_Project.exception.CustomerException;
import com.example.Spring_Coffee_Shop_Project.repository.*;
import com.example.Spring_Coffee_Shop_Project.service.HeldSaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeldSaleServiceImpl implements HeldSaleService {

    private final HeldSaleRepository heldSaleRepository;
    private final BatchRepository batchRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public HeldSaleDTO createHeldSale(HeldSaleCreateDTO dto) {
        log.info("Creating held sale...");

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new CustomerException(400, "Held sale must contain at least one item");
        }

        Batch batch = resolveBatch(dto.getBatchId());
        User cashier = getCurrentUser();
        Customer customer = resolveCustomer(dto.getCustomerId());

        HeldSale heldSale = new HeldSale();
        heldSale.setHoldCode(generateHoldCode());
        heldSale.setBatch(batch);
        heldSale.setCustomer(customer);
        heldSale.setHeldBy(cashier);
        heldSale.setNotes(dto.getNotes());
        heldSale.setRemovalRequested(false);

        BigDecimal subTotal = BigDecimal.ZERO;
        List<HeldSaleItem> items = new ArrayList<>();

        for (HeldSaleItemDTO lineDto : dto.getItems()) {
            if (lineDto.getQuantity() <= 0) {
                throw new CustomerException(400, "Quantity must be greater than 0");
            }

            Item item = itemRepository.findById(lineDto.getItemId())
                    .orElseThrow(() -> new CustomerException(404, "Item not found: " + lineDto.getItemId()));

            BigDecimal unitPrice = item.getDiscountPrice() != null ? item.getDiscountPrice() : item.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(lineDto.getQuantity()));

            HeldSaleItem heldItem = new HeldSaleItem();
            heldItem.setHeldSale(heldSale);
            heldItem.setItem(item);
            heldItem.setQuantity(lineDto.getQuantity());
            heldItem.setUnitPrice(unitPrice);
            heldItem.setLineTotal(lineTotal);
            heldItem.setNotes(lineDto.getNotes());
            items.add(heldItem);

            subTotal = subTotal.add(lineTotal);
        }

        heldSale.setHeldSaleItems(items);
        heldSale.setSubTotal(subTotal);
        heldSale.setTotalAmount(subTotal);

        HeldSale saved = heldSaleRepository.save(heldSale);
        log.info("Held sale created: {}", saved.getHoldCode());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HeldSaleDTO getHeldSaleById(long id) {
        HeldSale held = heldSaleRepository.findById(id)
                .orElseThrow(() -> new CustomerException(404, "Held sale not found: " + id));
        return mapToDTO(held);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeldSaleDTO> getAllHeldSales() {
        List<HeldSale> list = heldSaleRepository.findAllByOrderByHeldAtDesc();
        List<HeldSaleDTO> result = new ArrayList<>();
        for (HeldSale h : list) {
            result.add(mapToDTO(h));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeldSaleDTO> getHeldSalesByBatch(long batchId) {
        List<HeldSale> list = heldSaleRepository.findByBatchBatchId(batchId);
        List<HeldSaleDTO> result = new ArrayList<>();
        for (HeldSale h : list) {
            result.add(mapToDTO(h));
        }
        return result;
    }

    @Override
    @Transactional
    public HeldSaleDTO requestRemoval(long id) {
        HeldSale held = heldSaleRepository.findById(id)
                .orElseThrow(() -> new CustomerException(404, "Held sale not found: " + id));
        held.setRemovalRequested(true);
        HeldSale saved = heldSaleRepository.save(held);
        log.info("Removal requested for held sale: {}", saved.getHoldCode());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteHeldSale(long id) {
        HeldSale held = heldSaleRepository.findById(id)
                .orElseThrow(() -> new CustomerException(404, "Held sale not found: " + id));
        heldSaleRepository.delete(held);
        log.info("Held sale deleted: {}", held.getHoldCode());
    }

    private Batch resolveBatch(Long batchId) {
        if (batchId != null) {
            Batch batch = batchRepository.findById(batchId)
                    .orElseThrow(() -> new CustomerException(404, "Batch not found"));
            if (batch.getBatchStatus() != BatchStatus.OPEN) {
                throw new CustomerException(400, "Batch is not open");
            }
            return batch;
        }
        return batchRepository.findByBatchStatus(BatchStatus.OPEN)
                .orElseThrow(() -> new CustomerException(404, "No active batch open"));
    }

    private Customer resolveCustomer(Long customerId) {
        if (customerId == null) return null;
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerException(404, "Customer not found"));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerException(404, "User not found: " + username));
    }

    private String generateHoldCode() {
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "HOLD-" + prefix;
    }

    private HeldSaleDTO mapToDTO(HeldSale h) {
        List<HeldSaleItemDTO> itemDTOs = new ArrayList<>();
        for (HeldSaleItem it : h.getHeldSaleItems()) {
            itemDTOs.add(HeldSaleItemDTO.builder()
                    .heldSaleItemId(it.getHeldSaleItemId())
                    .itemId(it.getItem().getItemId())
                    .itemName(it.getItem().getItemName())
                    .quantity(it.getQuantity())
                    .unitPrice(it.getUnitPrice())
                    .lineTotal(it.getLineTotal())
                    .notes(it.getNotes())
                    .build());
        }

        return HeldSaleDTO.builder()
                .heldSaleId(h.getHeldSaleId())
                .holdCode(h.getHoldCode())
                .batchId(h.getBatch().getBatchId())
                .customerId(h.getCustomer() != null ? h.getCustomer().getCustomerId() : null)
                .customerName(h.getCustomer() != null
                        ? h.getCustomer().getFirstName() + " " + h.getCustomer().getLastName()
                        : null)
                .heldByUserId(h.getHeldBy().getUserId())
                .heldByName(h.getHeldBy().getFirstName() + " " + h.getHeldBy().getLastName())
                .subTotal(h.getSubTotal())
                .totalAmount(h.getTotalAmount())
                .notes(h.getNotes())
                .heldAt(h.getHeldAt())
                .removalRequested(h.isRemovalRequested())
                .heldSaleItems(itemDTOs)
                .build();
    }
}