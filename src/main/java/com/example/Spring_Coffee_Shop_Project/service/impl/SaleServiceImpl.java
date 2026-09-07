package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.*;
import com.example.Spring_Coffee_Shop_Project.entity.*;
import com.example.Spring_Coffee_Shop_Project.enumeration.*;
import com.example.Spring_Coffee_Shop_Project.exception.CustomerException;
import com.example.Spring_Coffee_Shop_Project.repository.*;
import com.example.Spring_Coffee_Shop_Project.service.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final BatchRepository batchRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final LoyaltyPointRepository loyaltyPointRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final SaleItemRepository saleItemRepository;

    @Override
    @Transactional
    public SaleDTO createSale(SaleCreateDTO saleCreateDTO) {
        log.info("Executing Create Sale (checkout)...");

        if (saleCreateDTO.getItems() == null || saleCreateDTO.getItems().isEmpty()) {
            throw new CustomerException(400, "Sale must contain at least one item");
        }

        Batch batch = resolveBatch(saleCreateDTO.getBatchId());
        User cashier = getCurrentUser();
        Customer customer = resolveCustomer(saleCreateDTO.getCustomerId());

        Sale sale = new Sale();
        sale.setSaleCode(generateSaleCode());
        sale.setBatch(batch);
        sale.setCustomer(customer);
        sale.setUser(cashier);
        sale.setNotes(saleCreateDTO.getNotes());
        sale.setSaleStatus(SaleStatus.PENDING);

        BigDecimal subTotal = BigDecimal.ZERO;
        List<SaleItem> saleItems = new ArrayList<>();

        for (SaleItemDTO lineDto : saleCreateDTO.getItems()) {
            if (lineDto.getQuantity() <= 0) {
                throw new CustomerException(400, "Quantity must be greater than 0 for item id: " + lineDto.getItemId());
            }

            Item item = itemRepository.findById(lineDto.getItemId())
                    .orElseThrow(() -> new CustomerException(404, "Item not found with id: " + lineDto.getItemId()));

            Stock stock = stockRepository.findByItem_ItemId(item.getItemId())
                    .orElseThrow(() -> new CustomerException(400, "No stock record found for item: " + item.getItemName()));

            if (stock.getAvailableQuantity() < lineDto.getQuantity()) {
                throw new CustomerException(400, "Insufficient stock for item: " + item.getItemName()
                        + ". Available: " + stock.getAvailableQuantity() + ", Requested: " + lineDto.getQuantity());
            }

            BigDecimal unitPrice = item.getPrice();
            BigDecimal discountPrice = item.getDiscountPrice();
            BigDecimal effectivePrice = discountPrice != null ? discountPrice : unitPrice;
            BigDecimal lineTotal = effectivePrice.multiply(BigDecimal.valueOf(lineDto.getQuantity()));

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setItem(item);
            saleItem.setQuantity(lineDto.getQuantity());
            saleItem.setUnitPrice(unitPrice);
            saleItem.setDiscountPrice(discountPrice);
            saleItem.setLineTotal(lineTotal);
            saleItem.setNotes(lineDto.getNotes());
            saleItems.add(saleItem);

            subTotal = subTotal.add(lineTotal);

            // Stock reduce
            stock.setAvailableQuantity(stock.getAvailableQuantity() - lineDto.getQuantity());
            stockRepository.save(stock);

            // Auto update item status
            if (stock.getAvailableQuantity() > 0) {
                item.setItemStatus(ItemStatus.AVAILABLE);
            } else {
                item.setItemStatus(ItemStatus.OUT_OF_STOCK);
            }
            itemRepository.save(item);

            StockTransaction stockTransaction = new StockTransaction();
            stockTransaction.setTransactionType(StockTransactionType.SALE);
            stockTransaction.setQuantity(lineDto.getQuantity());
            stockTransaction.setReason("Sale checkout - " + sale.getSaleCode());
            stockTransaction.setItem(item);
            stockTransaction.setPerformedBy(cashier);
            stockTransactionRepository.save(stockTransaction);
        }

        BigDecimal discountAmount = saleCreateDTO.getDiscountAmount() != null
                ? saleCreateDTO.getDiscountAmount()
                : BigDecimal.ZERO;

        if (discountAmount.compareTo(subTotal) > 0) {
            throw new CustomerException(400, "Discount amount cannot exceed subtotal");
        }

        BigDecimal totalAmount = subTotal.subtract(discountAmount);

        sale.setSaleItems(saleItems);
        sale.setSubTotal(subTotal);
        sale.setDiscountAmount(discountAmount);
        sale.setTotalAmount(totalAmount);

        if (saleCreateDTO.getPayment() != null) {
            Payment payment = buildPayment(sale, totalAmount, saleCreateDTO.getPayment());
            sale.setPayment(payment);
            sale.setSaleStatus(SaleStatus.COMPLETED);
            sale.setCompletedAt(LocalDateTime.now());
        }

        Sale savedSale = saleRepository.save(sale);

        if (savedSale.getSaleStatus() == SaleStatus.COMPLETED && savedSale.getCustomer() != null) {
            processLoyaltyPoints(savedSale, saleCreateDTO.getLoyaltyPointsUsed());
        }

        if (savedSale.getSaleStatus() == SaleStatus.COMPLETED) {
            batch.recordSale(totalAmount, totalAmount);
            batchRepository.save(batch);
        }

        log.info("Sale created successfully with code: {}", savedSale.getSaleCode());
        return mapToDTO(savedSale);
    }

    // ==================== GETTERS ====================

    @Override
    @Transactional(readOnly = true)
    public SaleDTO getSaleById(long id) {
        log.info("Fetching sale by id: {}", id);
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new CustomerException(404, "Sale not found with id: " + id));
        return mapToDTO(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleDTO> getAllSales() {
        log.info("Fetching all sales...");
        List<Sale> sales = saleRepository.findAllByOrderByCreatedAtDesc();
        List<SaleDTO> list = new ArrayList<>();
        for (Sale sale : sales) {
            list.add(mapToDTO(sale));
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByBatch(long batchId) {
        log.info("Fetching sales by batch id: {}", batchId);
        List<Sale> sales = saleRepository.findByBatchBatchId(batchId);
        List<SaleDTO> list = new ArrayList<>();
        for (Sale sale : sales) {
            list.add(mapToDTO(sale));
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByCustomer(long customerId) {
        log.info("Fetching sales by customer id: {}", customerId);
        List<Sale> sales = saleRepository.findByCustomerCustomerId(customerId);
        List<SaleDTO> list = new ArrayList<>();
        for (Sale sale : sales) {
            list.add(mapToDTO(sale));
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByStatus(SaleStatus status) {
        log.info("Fetching sales by status: {}", status);
        List<Sale> sales = saleRepository.findBySaleStatus(status);
        List<SaleDTO> list = new ArrayList<>();
        for (Sale sale : sales) {
            list.add(mapToDTO(sale));
        }
        return list;
    }

    // ==================== CANCEL SALE ====================

    @Override
    @Transactional
    public SaleDTO cancelSale(long id) {
        log.info("Cancelling sale id: {}", id);

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new CustomerException(404, "Sale not found with id: " + id));

        if (sale.getSaleStatus() == SaleStatus.CANCELLED) {
            throw new CustomerException(400, "Sale is already cancelled");
        }

        User currentUser = getCurrentUser();

        for (SaleItem saleItem : sale.getSaleItems()) {
            Item item = saleItem.getItem();

            Optional<Stock> optionalStock = stockRepository.findByItem_ItemId(item.getItemId());
            if (optionalStock.isPresent()) {
                Stock stock = optionalStock.get();
                stock.setAvailableQuantity(stock.getAvailableQuantity() + saleItem.getQuantity());
                stockRepository.save(stock);

                if (stock.getAvailableQuantity() > 0) {
                    item.setItemStatus(ItemStatus.AVAILABLE);
                } else {
                    item.setItemStatus(ItemStatus.OUT_OF_STOCK);
                }
                itemRepository.save(item);
            }

            StockTransaction stockTransaction = new StockTransaction();
            stockTransaction.setTransactionType(StockTransactionType.IN);
            stockTransaction.setQuantity(saleItem.getQuantity());
            stockTransaction.setReason("Sale cancelled - " + sale.getSaleCode());
            stockTransaction.setItem(item);
            stockTransaction.setPerformedBy(currentUser);
            stockTransactionRepository.save(stockTransaction);
        }

        if (sale.getPayment() != null) {
            sale.getPayment().setPaymentStatus(PaymentStatus.REFUNDED);
        }

        sale.setSaleStatus(SaleStatus.CANCELLED);
        Sale savedSale = saleRepository.save(sale);

        log.info("Sale cancelled successfully with code: {}", savedSale.getSaleCode());
        return mapToDTO(savedSale);
    }

    // ==================== SALES HISTORY ====================

    @Override
    @Transactional(readOnly = true)
    public SalesHistoryResponseDTO getSalesHistory(SalesHistoryFilterDTO filter) {
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (filter.getFromDate() != null) {
            fromDateTime = filter.getFromDate().atStartOfDay();
        }
        if (filter.getToDate() != null) {
            toDateTime = filter.getToDate().plusDays(1).atStartOfDay();
        }

        List<Sale> sales = saleRepository.findSalesHistory(
                filter.getBatchId(),
                filter.getYear(),
                filter.getMonth(),
                filter.getDay(),
                fromDateTime,
                toDateTime
        );

        // Sort
        String sort = filter.getSort() != null ? filter.getSort() : "newest";
        switch (sort) {
            case "oldest"  -> sales.sort(Comparator.comparing(Sale::getCreatedAt));
            case "highest" -> sales.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
            case "lowest"  -> sales.sort(Comparator.comparing(Sale::getTotalAmount));
            default        -> sales.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }

        List<SalesHistoryItemDTO> historyItems = sales.stream()
                .map(this::mapToHistoryItem)
                .collect(Collectors.toList());

        // Summary
        BigDecimal totalRevenue = historyItems.stream()
                .map(SalesHistoryItemDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = historyItems.stream()
                .map(SalesHistoryItemDTO::getProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profitPct = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalProfit.multiply(BigDecimal.valueOf(100))
                .divide(totalRevenue, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long uniqueCustomers = historyItems.stream()
                .map(SalesHistoryItemDTO::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        SalesHistorySummaryDTO summary = SalesHistorySummaryDTO.builder()
                .totalSales(historyItems.size())
                .totalRevenue(totalRevenue)
                .totalProfit(totalProfit)
                .profitPercentage(profitPct)
                .totalCustomers(uniqueCustomers)
                .flaggedCount(0)
                .build();

        // Top Items
        Map<String, TopItemDTO> topMap = new LinkedHashMap<>();
        for (Sale sale : sales) {
            for (SaleItem si : sale.getSaleItems()) {
                String name = si.getItem().getItemName();
                topMap.compute(name, (k, v) -> {
                    if (v == null) {
                        return TopItemDTO.builder()
                                .itemName(name)
                                .quantity(si.getQuantity())
                                .revenue(si.getLineTotal())
                                .build();
                    }
                    v.setQuantity(v.getQuantity() + si.getQuantity());
                    v.setRevenue(v.getRevenue().add(si.getLineTotal()));
                    return v;
                });
            }
        }

        List<TopItemDTO> topItems = topMap.values().stream()
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(6)
                .collect(Collectors.toList());

        return SalesHistoryResponseDTO.builder()
                .summary(summary)
                .topItems(topItems)
                .sales(historyItems)
                .build();
    }

    // ==================== PRIVATE HELPERS ====================

    private SalesHistoryItemDTO mapToHistoryItem(Sale sale) {
        List<SaleItemDTO> itemDTOs = new ArrayList<>();
        int totalQty = 0;

        for (SaleItem si : sale.getSaleItems()) {
            totalQty += si.getQuantity();
            itemDTOs.add(SaleItemDTO.builder()
                    .saleItemId(si.getSaleItemId())
                    .itemId(si.getItem().getItemId())
                    .itemName(si.getItem().getItemName())
                    .quantity(si.getQuantity())
                    .unitPrice(si.getUnitPrice())
                    .discountPrice(si.getDiscountPrice())
                    .lineTotal(si.getLineTotal())
                    .notes(si.getNotes())
                    .build());
        }

        String itemSummary;
        if (itemDTOs.size() == 1) {
            SaleItemDTO only = itemDTOs.get(0);
            itemSummary = only.getQuantity() > 1
                    ? only.getItemName() + " x" + only.getQuantity()
                    : only.getItemName();
        } else {
            itemSummary = itemDTOs.size() + " Items";
        }

        int pointsEarned = 0;
        if (sale.getCustomer() != null) {
            List<LoyaltyTransaction> txs = loyaltyTransactionRepository.findBySaleSaleId(sale.getSaleId());
            pointsEarned = txs.stream()
                    .filter(tx -> tx.getTransactionType() == LoyaltyTransactionType.EARNED)
                    .mapToInt(LoyaltyTransaction::getPoints)
                    .sum();
        }

        BigDecimal profit = sale.getTotalAmount()
                .multiply(BigDecimal.valueOf(0.42))
                .setScale(0, RoundingMode.HALF_UP);

        String customerName = null;
        String customerPhone = null;
        Long customerId = null;
        if (sale.getCustomer() != null) {
            customerId = sale.getCustomer().getCustomerId();
            customerName = sale.getCustomer().getFirstName() + " " + sale.getCustomer().getLastName();
            customerPhone = sale.getCustomer().getPhone();
        }

        return SalesHistoryItemDTO.builder()
                .saleId(sale.getSaleId())
                .saleCode(sale.getSaleCode())
                .customerId(customerId)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .batchId(sale.getBatch().getBatchId())
                .batchCode(sale.getBatch().getBatchCode())
                .itemSummary(itemSummary)
                .totalQty(totalQty)
                .totalAmount(sale.getTotalAmount())
                .profit(profit)
                .pointsEarned(pointsEarned)
                .flagged(false)
                .createdAt(sale.getCreatedAt())
                .saleItems(itemDTOs)
                .build();
    }

    private Batch resolveBatch(Long batchId) {
        if (batchId != null) {
            Batch batch = batchRepository.findById(batchId)
                    .orElseThrow(() -> new CustomerException(404, "Batch not found with id: " + batchId));
            if (batch.getBatchStatus() != BatchStatus.OPEN) {
                throw new CustomerException(400, "Batch " + batch.getBatchCode() + " is not open");
            }
            return batch;
        }
        return batchRepository.findByBatchStatus(BatchStatus.OPEN)
                .orElseThrow(() -> new CustomerException(404, "No active batch is open. Please open a batch first."));
    }

    private Customer resolveCustomer(Long customerId) {
        if (customerId == null) return null;
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerException(404, "Customer not found with id: " + customerId));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerException(404, "User not found: " + username));
    }

    private String generateSaleCode() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "SALE-" + datePrefix;
    }

    private Payment buildPayment(Sale sale, BigDecimal totalAmount, PaymentCreateDTO paymentCreateDTO) {
        if (paymentCreateDTO.getPaymentDetails() == null || paymentCreateDTO.getPaymentDetails().isEmpty()) {
            throw new CustomerException(400, "Payment must include at least one payment detail");
        }

        BigDecimal sumOfDetails = BigDecimal.ZERO;
        List<PaymentDetail> paymentDetails = new ArrayList<>();

        Payment payment = new Payment();
        payment.setSale(sale);

        for (PaymentDetailDTO detailDTO : paymentCreateDTO.getPaymentDetails()) {
            if (detailDTO.getAmount() == null || detailDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomerException(400, "Payment detail amount must be greater than 0");
            }

            PaymentDetail paymentDetail = new PaymentDetail();
            paymentDetail.setPayment(payment);
            paymentDetail.setPaymentMethod(detailDTO.getPaymentMethod());
            paymentDetail.setAmount(detailDTO.getAmount());
            paymentDetail.setReference(detailDTO.getReference());
            paymentDetails.add(paymentDetail);

            sumOfDetails = sumOfDetails.add(detailDTO.getAmount());
        }

        BigDecimal totalPaid = paymentCreateDTO.getTotalPaid() != null
                ? paymentCreateDTO.getTotalPaid()
                : sumOfDetails;

        if (totalPaid.compareTo(totalAmount) < 0) {
            throw new CustomerException(400, "Total paid (" + totalPaid + ") is less than the sale total (" + totalAmount + ")");
        }

        BigDecimal changeAmount = paymentCreateDTO.getChangeAmount() != null
                ? paymentCreateDTO.getChangeAmount()
                : totalPaid.subtract(totalAmount);

        payment.setTotalPaid(totalPaid);
        payment.setChangeAmount(changeAmount);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDetails(paymentDetails);

        return payment;
    }

    private void processLoyaltyPoints(Sale sale, Integer loyaltyPointsUsed) {
        Customer customer = sale.getCustomer();
        if (customer == null) return;

        boolean alreadyEarned = loyaltyTransactionRepository
                .findByCustomerCustomerId(customer.getCustomerId())
                .stream()
                .anyMatch(tx -> tx.getSale() != null
                        && tx.getSale().getSaleId() == sale.getSaleId()
                        && tx.getTransactionType() == LoyaltyTransactionType.EARNED);

        if (alreadyEarned) {
            log.warn("Loyalty points already processed for sale: {}. Skipping.", sale.getSaleCode());
            return;
        }

        LoyaltyPoint loyaltyPoint = loyaltyPointRepository
                .findByCustomerCustomerId(customer.getCustomerId())
                .orElseGet(() -> {
                    LoyaltyPoint lp = new LoyaltyPoint();
                    lp.setCustomer(customer);
                    lp.setAvailablePoints(0);
                    lp.setTotalEarned(0);
                    lp.setTotalRedeemed(0);
                    return loyaltyPointRepository.save(lp);
                });

        int pointsToEarn = sale.getTotalAmount()
                .divide(BigDecimal.valueOf(50), 0, RoundingMode.FLOOR)
                .intValue();

        int pointsUsed = (loyaltyPointsUsed != null) ? loyaltyPointsUsed : 0;

        if (pointsUsed > 0) {
            if (loyaltyPoint.getAvailablePoints() < pointsUsed) {
                throw new CustomerException(400, "Not enough loyalty points. Available: "
                        + loyaltyPoint.getAvailablePoints());
            }

            loyaltyPoint.setAvailablePoints(loyaltyPoint.getAvailablePoints() - pointsUsed);
            loyaltyPoint.setTotalRedeemed(loyaltyPoint.getTotalRedeemed() + pointsUsed);

            LoyaltyTransaction redeemTx = new LoyaltyTransaction();
            redeemTx.setCustomer(customer);
            redeemTx.setSale(sale);
            redeemTx.setTransactionType(LoyaltyTransactionType.REDEEMED);
            redeemTx.setPoints(pointsUsed);
            redeemTx.setDescription("Points redeemed on sale " + sale.getSaleCode());
            loyaltyTransactionRepository.save(redeemTx);
        }

        if (pointsToEarn > 0) {
            loyaltyPoint.setAvailablePoints(loyaltyPoint.getAvailablePoints() + pointsToEarn);
            loyaltyPoint.setTotalEarned(loyaltyPoint.getTotalEarned() + pointsToEarn);

            LoyaltyTransaction earnTx = new LoyaltyTransaction();
            earnTx.setCustomer(customer);
            earnTx.setSale(sale);
            earnTx.setTransactionType(LoyaltyTransactionType.EARNED);
            earnTx.setPoints(pointsToEarn);
            earnTx.setDescription("Points earned from sale " + sale.getSaleCode());
            loyaltyTransactionRepository.save(earnTx);
        }

        loyaltyPointRepository.save(loyaltyPoint);

        log.info("Loyalty points processed for customer {}: earned={}, redeemed={}",
                customer.getCustomerId(), pointsToEarn, pointsUsed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemSalesChartDTO> getItemSalesChart(String period, String category, Long batchId) {

        LocalDateTime fromDate = null;
        LocalDateTime toDate   = null;
        LocalDateTime now = LocalDateTime.now();

        if ("today".equalsIgnoreCase(period)) {
            fromDate = now.toLocalDate().atStartOfDay();
            toDate   = fromDate.plusDays(1);
        } else if ("month".equalsIgnoreCase(period)) {
            fromDate = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            toDate   = fromDate.plusMonths(1);
        }
        String catFilter = (category == null || category.isBlank()) ? null : category.trim();

        List<Object[]> rows = saleItemRepository.findItemSalesChart(
                fromDate, toDate, catFilter, batchId
        );

        List<ItemSalesChartDTO> result = new ArrayList<>();

        for (Object[] row : rows) {
            String itemName     = (String) row[0];
            String categoryName = (String) row[1];
            Long unitsLong      = (Long) row[2];
            BigDecimal revenue  = (BigDecimal) row[3];

            int units = unitsLong != null ? unitsLong.intValue() : 0;
            if (revenue == null) revenue = BigDecimal.ZERO;

            // Same 42% profit logic used in sales history
            BigDecimal profit = revenue
                    .multiply(BigDecimal.valueOf(0.42))
                    .setScale(0, RoundingMode.HALF_UP);

            result.add(ItemSalesChartDTO.builder()
                    .name(itemName)
                    .category(categoryName)
                    .units(units)
                    .revenue(revenue)
                    .profit(profit)
                    .build());
        }

        return result;
    }

    private SaleDTO mapToDTO(Sale sale) {
        List<SaleItemDTO> itemDTOs = new ArrayList<>();
        for (SaleItem saleItem : sale.getSaleItems()) {
            itemDTOs.add(SaleItemDTO.builder()
                    .saleItemId(saleItem.getSaleItemId())
                    .itemId(saleItem.getItem().getItemId())
                    .itemName(saleItem.getItem().getItemName())
                    .quantity(saleItem.getQuantity())
                    .unitPrice(saleItem.getUnitPrice())
                    .discountPrice(saleItem.getDiscountPrice())
                    .lineTotal(saleItem.getLineTotal())
                    .notes(saleItem.getNotes())
                    .build());
        }

        PaymentDTO paymentDTO = null;
        if (sale.getPayment() != null) {
            Payment payment = sale.getPayment();
            List<PaymentDetailDTO> detailDTOs = new ArrayList<>();
            for (PaymentDetail detail : payment.getPaymentDetails()) {
                detailDTOs.add(PaymentDetailDTO.builder()
                        .paymentDetailId(detail.getPaymentDetailId())
                        .paymentMethod(detail.getPaymentMethod())
                        .amount(detail.getAmount())
                        .reference(detail.getReference())
                        .build());
            }

            paymentDTO = PaymentDTO.builder()
                    .paymentId(payment.getPaymentId())
                    .totalPaid(payment.getTotalPaid())
                    .changeAmount(payment.getChangeAmount())
                    .paymentStatus(payment.getPaymentStatus())
                    .paidAt(payment.getPaidAt())
                    .paymentDetails(detailDTOs)
                    .build();
        }

        return SaleDTO.builder()
                .saleId(sale.getSaleId())
                .saleCode(sale.getSaleCode())
                .batchId(sale.getBatch().getBatchId())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getCustomerId() : null)
                .customerName(sale.getCustomer() != null
                        ? sale.getCustomer().getFirstName() + " " + sale.getCustomer().getLastName()
                        : null)
                .userId(sale.getUser().getUserId())
                .cashierName(sale.getUser().getFirstName() + " " + sale.getUser().getLastName())
                .saleStatus(sale.getSaleStatus())
                .subTotal(sale.getSubTotal())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .notes(sale.getNotes())
                .createdAt(sale.getCreatedAt())
                .completedAt(sale.getCompletedAt())
                .saleItems(itemDTOs)
                .payment(paymentDTO)
                .build();
    }
}