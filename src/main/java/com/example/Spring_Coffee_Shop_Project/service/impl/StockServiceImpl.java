package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.StockAdjustDTO;
import com.example.Spring_Coffee_Shop_Project.dto.StockDTO;
import com.example.Spring_Coffee_Shop_Project.dto.StockTransactionDTO;
import com.example.Spring_Coffee_Shop_Project.entity.Item;
import com.example.Spring_Coffee_Shop_Project.entity.Stock;
import com.example.Spring_Coffee_Shop_Project.entity.StockTransaction;
import com.example.Spring_Coffee_Shop_Project.entity.User;
import com.example.Spring_Coffee_Shop_Project.enumeration.ItemStatus;
import com.example.Spring_Coffee_Shop_Project.exception.CustomerException;
import com.example.Spring_Coffee_Shop_Project.repository.ItemRepository;
import com.example.Spring_Coffee_Shop_Project.repository.StockRepository;
import com.example.Spring_Coffee_Shop_Project.repository.StockTransactionRepository;
import com.example.Spring_Coffee_Shop_Project.repository.UserRepository;
import com.example.Spring_Coffee_Shop_Project.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockTransactionRepository transactionRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    @Override
    public List<StockDTO> getAllStocks() {
        log.info("Fetching all stocks...");
        List<Stock> stocks = stockRepository.findAllWithItemAndCategory();
        List<StockDTO> stockDTOList = new ArrayList<>();

        for (Stock stock : stocks) {
            stockDTOList.add(mapToDTO(stock));
        }
        return stockDTOList;
    }

    @Transactional(readOnly = true)
    @Override
    public StockDTO getStockById(Long id) {
        log.info("Fetching stock by id: {}", id);

        Optional<Stock> optionalStock = stockRepository.findById(id);

        if (optionalStock.isEmpty()) {
            throw new CustomerException(404, "Stock not found with id: " + id);
        }

        return mapToDTO(optionalStock.get());
    }

    @Transactional(readOnly = true)
    @Override
    public StockDTO getStockByItemId(Long itemId) {
        log.info("Fetching stock by itemId: {}", itemId);

        Optional<Stock> optionalStock = stockRepository.findByItem_ItemId(itemId);

        if (optionalStock.isEmpty()) {
            throw new CustomerException(404, "Stock not found for item id: " + itemId);
        }

        return mapToDTO(optionalStock.get());
    }

    @Transactional(readOnly = true)
    @Override
    public List<StockDTO> getLowStockItems() {
        log.info("Fetching low stock items...");
        List<Stock> stocks = stockRepository.findLowStockItems();
        List<StockDTO> stockDTOList = new ArrayList<>();

        for (Stock stock : stocks) {
            stockDTOList.add(mapToDTO(stock));
        }
        return stockDTOList;
    }

    @Transactional(readOnly = true)
    @Override
    public List<StockDTO> getOutOfStockItems() {
        log.info("Fetching out of stock items...");
        List<Stock> stocks = stockRepository.findOutOfStockItems();
        List<StockDTO> stockDTOList = new ArrayList<>();

        for (Stock stock : stocks) {
            stockDTOList.add(mapToDTO(stock));
        }
        return stockDTOList;
    }


    @Transactional(readOnly = true)
    @Override
    public StockStatistics getStockStatistics() {
        log.info("Calculating stock statistics...");

        long totalItems = stockRepository.countTotalStockRecords();
        long lowStock = stockRepository.findLowStockItems().size();
        long outOfStock = stockRepository.findOutOfStockItems().size();
        Integer totalUnits = stockRepository.sumTotalAvailableQuantity();

        return StockStatistics.builder()
                .totalItems(totalItems)
                .lowStock(lowStock)
                .outOfStock(outOfStock)
                .totalUnits(totalUnits != null ? totalUnits : 0)
                .build();
    }

    @Transactional
    @Override
    public StockTransactionDTO adjustStock(StockAdjustDTO adjustDTO) {
        log.info("Adjusting stock for itemId: {}, type: {}, quantity: {}",
                adjustDTO.getItemId(), adjustDTO.getTransactionType(), adjustDTO.getQuantity());

        if (adjustDTO.getQuantity() <= 0) {
            throw new CustomerException(400, "Quantity must be greater than 0");
        }

        Optional<Item> optionalItem = itemRepository.findById(adjustDTO.getItemId());
        if (optionalItem.isEmpty()) {
            throw new CustomerException(404, "Item not found with id: " + adjustDTO.getItemId());
        }
        Item item = optionalItem.get();

        Optional<Stock> optionalStock = stockRepository.findByItem_ItemId(item.getItemId());
        Stock stock;

        if (optionalStock.isPresent()) {
            stock = optionalStock.get();
        } else {
            stock = new Stock();
            stock.setItem(item);
            stock.setAvailableQuantity(0);
            stock.setReorderLevel(10);
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new CustomerException(404, "User not found: " + username);
        }
        User performedBy = optionalUser.get();

        int oldQuantity = stock.getAvailableQuantity();
        int newQuantity = calculateNewQuantity(oldQuantity, adjustDTO);

        stock.setAvailableQuantity(newQuantity);
        stockRepository.save(stock);

        if (newQuantity > 0) {
            item.setItemStatus(ItemStatus.AVAILABLE);
        } else {
            item.setItemStatus(ItemStatus.OUT_OF_STOCK);
        }
        itemRepository.save(item);
        log.info("Item status auto-updated to: {}", item.getItemStatus());

        StockTransaction transaction = new StockTransaction();
        transaction.setTransactionType(adjustDTO.getTransactionType());
        transaction.setQuantity(adjustDTO.getQuantity());
        transaction.setReason(adjustDTO.getReason());
        transaction.setItem(item);
        transaction.setPerformedBy(performedBy);
        transaction.setTransactionDate(LocalDateTime.now());

        StockTransaction savedTransaction = transactionRepository.save(transaction);

        log.info("Stock adjusted: item={}, oldQty={}, newQty={}, type={}, qty={}",
                item.getItemName(), oldQuantity, newQuantity,
                adjustDTO.getTransactionType(), adjustDTO.getQuantity());

        return mapToTransactionDTO(savedTransaction);
    }

    private int calculateNewQuantity(int currentQuantity, StockAdjustDTO adjustDTO) {
        switch (adjustDTO.getTransactionType()) {
            case IN:
                return currentQuantity + adjustDTO.getQuantity();

            case OUT:
            case SALE:
            case WASTE:
                if (currentQuantity < adjustDTO.getQuantity()) {
                    throw new CustomerException(400,
                            "Insufficient stock. Available: " + currentQuantity +
                                    ", Requested removal: " + adjustDTO.getQuantity());
                }
                return currentQuantity - adjustDTO.getQuantity();

            case ADJUSTMENT:
                return adjustDTO.getQuantity();

            default:
                throw new CustomerException(400, "Invalid transaction type");
        }
    }

    @Transactional
    @Override
    public StockDTO initializeStock(Long itemId, int initialQuantity, int reorderLevel) {
        log.info("Initializing stock for itemId: {}", itemId);

        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            throw new CustomerException(404, "Item not found with id: " + itemId);
        }

        if (stockRepository.existsByItem_ItemId(itemId)) {
            throw new CustomerException(409, "Stock already exists for this item");
        }

        Stock stock = new Stock();
        stock.setItem(optionalItem.get());
        stock.setAvailableQuantity(initialQuantity);
        stock.setReorderLevel(reorderLevel);
        stock.setMaxStockLevel(100);

        Stock savedStock = stockRepository.save(stock);
        return mapToDTO(savedStock);
    }

    @Transactional
    @Override
    public StockDTO updateReorderLevel(Long stockId, int newReorderLevel) {
        log.info("Updating reorder level for stockId: {}", stockId);

        Optional<Stock> optionalStock = stockRepository.findById(stockId);
        if (optionalStock.isEmpty()) {
            throw new CustomerException(404, "Stock not found with id: " + stockId);
        }

        if (newReorderLevel < 0) {
            throw new CustomerException(400, "Reorder level cannot be negative");
        }

        Stock stock = optionalStock.get();
        stock.setReorderLevel(newReorderLevel);

        Stock updatedStock = stockRepository.save(stock);
        return mapToDTO(updatedStock);
    }

    @Transactional
    @Override
    public StockDTO updateMaxStockLevel(Long stockId, int maxStockLevel) {
        log.info("Updating max stock level for stockId: {}", stockId);

        Optional<Stock> optionalStock = stockRepository.findById(stockId);
        if (optionalStock.isEmpty()) {
            throw new CustomerException(404, "Stock not found with id: " + stockId);
        }

        if (maxStockLevel < 0) {
            throw new CustomerException(400, "Max stock level cannot be negative");
        }

        Stock stock = optionalStock.get();
        stock.setMaxStockLevel(maxStockLevel);

        Stock updatedStock = stockRepository.save(stock);
        return mapToDTO(updatedStock);
    }

    @Transactional(readOnly = true)
    @Override
    public List<StockTransactionDTO> getAllTransactions() {
        log.info("Fetching all stock transactions...");

        List<StockTransaction> transactions = transactionRepository.findAll();
        List<StockTransactionDTO> dtoList = new ArrayList<>();

        for (StockTransaction t : transactions) {
            dtoList.add(mapToTransactionDTO(t));
        }
        return dtoList;
    }

    private StockDTO mapToDTO(Stock stock) {
        return StockDTO.builder()
                .stockId(stock.getStockId())
                .itemId(stock.getItem().getItemId())
                .itemName(stock.getItem().getItemName())
                .availableQuantity(stock.getAvailableQuantity())
                .reorderLevel(stock.getReorderLevel())
                .categoryName(stock.getItem().getCategory() != null ?
                        stock.getItem().getCategory().getCategoryName() : null)
                .maxStockLevel(stock.getMaxStockLevel())
                .build();
    }

    private StockTransactionDTO mapToTransactionDTO(StockTransaction transaction) {
        return StockTransactionDTO.builder()
                .stockTransactionId(transaction.getStockTransactionId())
                .transactionType(transaction.getTransactionType())
                .quantity(transaction.getQuantity())
                .reason(transaction.getReason())
                .transactionDate(transaction.getTransactionDate())
                .itemId(transaction.getItem().getItemId())
                .itemName(transaction.getItem().getItemName())
                .performedByUserId(transaction.getPerformedBy() != null ?
                        transaction.getPerformedBy().getUserId() : null)
                .performedByName(transaction.getPerformedBy() != null ?
                        transaction.getPerformedBy().getFirstName() + " " +
                                transaction.getPerformedBy().getLastName() : null)
                .build();
    }

    @lombok.Builder
    @lombok.Getter
    public static class StockStatistics {
        private long totalItems;
        private long lowStock;
        private long outOfStock;
        private int totalUnits;
    }
}