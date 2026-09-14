package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.*;
import com.example.Spring_Coffee_Shop_Project.entity.Item;
import com.example.Spring_Coffee_Shop_Project.entity.SupplierInvoice;
import com.example.Spring_Coffee_Shop_Project.entity.SupplierInvoiceItem;
import com.example.Spring_Coffee_Shop_Project.entity.User;
import com.example.Spring_Coffee_Shop_Project.enumeration.InvoiceStatus;
import com.example.Spring_Coffee_Shop_Project.enumeration.StockTransactionType;
import com.example.Spring_Coffee_Shop_Project.exception.CustomerException;
import com.example.Spring_Coffee_Shop_Project.repository.ItemRepository;
import com.example.Spring_Coffee_Shop_Project.repository.SupplierInvoiceRepository;
import com.example.Spring_Coffee_Shop_Project.repository.UserRepository;
import com.example.Spring_Coffee_Shop_Project.service.InvoiceService;
import com.example.Spring_Coffee_Shop_Project.service.StockService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final SupplierInvoiceRepository invoiceRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-3.8-flash}")
    private String geminiModel;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    // Minimum score required for automatic item matching
    private static final double MATCH_THRESHOLD = 0.55;


    @Override
    public SupplierInvoiceDTO scanInvoice(MultipartFile file) {

        // Check uploaded file
        if (file == null || file.isEmpty()) {
            throw new CustomerException(
                    400,
                    "No image file provided"
            );
        }

        // Check Gemini API key
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new CustomerException(
                    500,
                    "GEMINI_API_KEY is not configured on the server"
            );
        }

        // Save invoice image
        String imageUrl = saveInvoiceImage(file);

        // Get image MIME type
        String mediaType = file.getContentType();

        if (mediaType == null || mediaType.isBlank()) {
            mediaType = "image/jpeg";
        }

        JsonNode extracted;

        try {

            // Convert image to Base64
            String base64Image = Base64.getEncoder()
                    .encodeToString(file.getBytes());

            // Send image to Gemini
            extracted = callGeminiForExtraction(
                    base64Image,
                    mediaType
            );

        } catch (IOException e) {

            throw new CustomerException(
                    500,
                    "Could not read uploaded image: " + e.getMessage()
            );
        }


        // =====================================================
        // Create Supplier Invoice
        // =====================================================

        SupplierInvoice invoice = new SupplierInvoice();

        invoice.setImageUrl(imageUrl);

        invoice.setStatus(
                InvoiceStatus.PENDING_REVIEW
        );

        invoice.setSupplierName(
                textOrNull(
                        extracted,
                        "supplierName"
                )
        );

        invoice.setInvoiceDate(
                dateOrNull(
                        extracted,
                        "invoiceDate"
                )
        );

        invoice.setTotalAmount(
                decimalOrNull(
                        extracted,
                        "totalAmount"
                )
        );


        // =====================================================
        // Match Extracted Items With Existing Catalog Items
        // =====================================================

        List<Item> catalog = itemRepository.findAll();

        List<SupplierInvoiceItem> lineItems =
                new ArrayList<>();

        JsonNode itemsNode =
                extracted.path("items");

        if (itemsNode.isArray()) {

            for (JsonNode itemNode : itemsNode) {

                String name =
                        textOrNull(
                                itemNode,
                                "name"
                        );

                // Skip item if name cannot be read
                if (name == null || name.isBlank()) {
                    continue;
                }


                SupplierInvoiceItem line =
                        new SupplierInvoiceItem();

                line.setInvoice(invoice);

                line.setExtractedItemName(name);


                // Quantity
                int quantity =
                        itemNode
                                .path("quantity")
                                .asInt(1);

                if (quantity <= 0) {
                    quantity = 1;
                }

                line.setQuantity(quantity);


                // Unit price
                BigDecimal unitPrice =
                        decimalOrNull(
                                itemNode,
                                "unitPrice"
                        );

                if (unitPrice == null) {
                    unitPrice = BigDecimal.ZERO;
                }

                line.setUnitPrice(unitPrice);


                // =================================================
                // Find Best Existing Item
                // =================================================

                Item bestMatch = null;

                double bestScore = 0;


                for (Item candidate : catalog) {

                    if (candidate.getItemName() == null) {
                        continue;
                    }

                    double score =
                            similarity(
                                    name,
                                    candidate.getItemName()
                            );

                    if (score > bestScore) {

                        bestScore = score;

                        bestMatch = candidate;
                    }
                }


                // =================================================
                // Save Match If Score Is Good Enough
                // =================================================

                if (bestMatch != null &&
                        bestScore >= MATCH_THRESHOLD) {

                    line.setMatchedItem(
                            bestMatch
                    );

                    line.setMatchConfidence(
                            bestScore
                    );

                } else {

                    // No reliable match
                    line.setMatchConfidence(
                            bestScore
                    );
                }


                lineItems.add(line);
            }
        }


        invoice.setItems(lineItems);


        // Save invoice
        SupplierInvoice saved =
                invoiceRepository.save(invoice);


        return mapToDTO(saved);
    }


    // =========================================================
    // CONFIRM INVOICE
    // =========================================================

    @Override
    public SupplierInvoiceDTO confirmInvoice(
            long invoiceId,
            InvoiceConfirmDTO confirmDTO) {

        SupplierInvoice invoice =
                invoiceRepository.findByIdWithItems(
                        invoiceId
                );


        if (invoice == null) {

            throw new CustomerException(
                    404,
                    "Invoice not found: " + invoiceId
            );
        }


        // Invoice must be pending
        if (invoice.getStatus() !=
                InvoiceStatus.PENDING_REVIEW) {

            throw new CustomerException(
                    400,
                    "Invoice is already " +
                            invoice.getStatus()
            );
        }


        // At least one item required
        if (confirmDTO.getItems() == null ||
                confirmDTO.getItems().isEmpty()) {

            throw new CustomerException(
                    400,
                    "At least one item is required to confirm an invoice"
            );
        }


        // Update supplier name
        if (confirmDTO.getSupplierName() != null) {

            invoice.setSupplierName(
                    confirmDTO.getSupplierName()
            );
        }


        // Update invoice date
        if (confirmDTO.getInvoiceDate() != null) {

            invoice.setInvoiceDate(
                    confirmDTO.getInvoiceDate()
            );
        }


        // =====================================================
        // Process Each Invoice Item
        // =====================================================

        for (InvoiceConfirmItemDTO editedLine :
                confirmDTO.getItems()) {


            SupplierInvoiceItem line =
                    invoice.getItems()
                            .stream()
                            .filter(i ->
                                    i.getInvoiceItemId()
                                            ==
                                            editedLine.getInvoiceItemId()
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new CustomerException(
                                            400,
                                            "Line item " +
                                                    editedLine.getInvoiceItemId() +
                                                    " does not belong to this invoice"
                                    )
                            );


            // =================================================
            // Catalog Item Must Be Selected
            // =================================================

            if (editedLine.getMatchedItemId() == null) {

                throw new CustomerException(
                        400,
                        "Please map \"" +
                                line.getExtractedItemName() +
                                "\" to a catalog item before confirming"
                );
            }


            // Find selected catalog item
            Item matchedItem =
                    itemRepository.findById(
                            editedLine.getMatchedItemId()
                    ).orElseThrow(() ->
                            new CustomerException(
                                    404,
                                    "Item not found: " +
                                            editedLine.getMatchedItemId()
                            )
                    );


            // Update invoice item
            line.setMatchedItem(
                    matchedItem
            );

            line.setQuantity(
                    editedLine.getQuantity()
            );


            if (editedLine.getUnitPrice() != null) {

                line.setUnitPrice(
                        editedLine.getUnitPrice()
                );
            }


            // =================================================
            // ADD STOCK
            // =================================================

            stockService.adjustStock(
                    StockAdjustDTO.builder()
                            .itemId(
                                    matchedItem.getItemId()
                            )
                            .transactionType(
                                    StockTransactionType.IN
                            )
                            .quantity(
                                    editedLine.getQuantity()
                            )
                            .reason(
                                    "Invoice scan" +
                                            (
                                                    invoice.getSupplierName()
                                                            != null
                                                            ?
                                                            " - " +
                                                                    invoice.getSupplierName()
                                                            :
                                                            ""
                                            )
                            )
                            .build()
            );
        }


        // =====================================================
        // Mark Invoice As Confirmed
        // =====================================================

        invoice.setStatus(
                InvoiceStatus.CONFIRMED
        );

        invoice.setConfirmedAt(
                LocalDateTime.now()
        );


        // User who confirmed
        if (confirmDTO.getPerformedByUserId() != null) {

            User user =
                    userRepository.findById(
                            confirmDTO.getPerformedByUserId()
                    ).orElse(null);

            invoice.setConfirmedBy(user);
        }


        SupplierInvoice saved =
                invoiceRepository.save(invoice);


        return mapToDTO(saved);
    }


    // =========================================================
    // REJECT INVOICE
    // =========================================================

    @Override
    public SupplierInvoiceDTO rejectInvoice(
            long invoiceId) {

        SupplierInvoice invoice =
                invoiceRepository.findById(invoiceId)
                        .orElseThrow(() ->
                                new CustomerException(
                                        404,
                                        "Invoice not found: " +
                                                invoiceId
                                )
                        );


        invoice.setStatus(
                InvoiceStatus.REJECTED
        );


        return mapToDTO(
                invoiceRepository.save(invoice)
        );
    }


    // =========================================================
    // GET SINGLE INVOICE
    // =========================================================

    @Override
    public SupplierInvoiceDTO getInvoice(
            long invoiceId) {

        SupplierInvoice invoice =
                invoiceRepository.findByIdWithItems(
                        invoiceId
                );


        if (invoice == null) {

            throw new CustomerException(
                    404,
                    "Invoice not found: " +
                            invoiceId
            );
        }


        return mapToDTO(invoice);
    }


    // =========================================================
    // GET ALL INVOICES
    // =========================================================

    @Override
    public List<SupplierInvoiceDTO> getAllInvoices() {

        List<SupplierInvoiceDTO> result =
                new ArrayList<>();


        for (SupplierInvoice invoice :
                invoiceRepository
                        .findAllByOrderByCreatedAtDesc()) {

            result.add(
                    mapToDTO(invoice)
            );
        }


        return result;
    }


    // =========================================================
    // GEMINI AI INVOICE EXTRACTION
    // =========================================================

    private JsonNode callGeminiForExtraction(
            String base64Image,
            String mediaType) {

        String prompt = """
                You are reading a supplier invoice or receipt
                for a coffee shop.

                Extract the following information:

                1. Supplier name
                2. Invoice date
                3. Total amount
                4. All invoice items
                5. Quantity of each item
                6. Unit price of each item

                Return ONLY valid JSON.

                Use exactly this structure:

                {
                  "supplierName": "string or null",
                  "invoiceDate": "YYYY-MM-DD or null",
                  "totalAmount": number or null,
                  "items": [
                    {
                      "name": "string",
                      "quantity": number,
                      "unitPrice": number
                    }
                  ]
                }

                Rules:

                - Do not invent information.
                - If a value cannot be read, use null.
                - Quantity should be 1 if it is not clearly stated.
                - Read low-quality images as accurately as possible.
                - Do not add explanations.
                - Do not use markdown.
                - Return only JSON.
                """;


        try {

            // =================================================
            // Root JSON
            // =================================================

            var root =
                    objectMapper.createObjectNode();


            // =================================================
            // Contents
            // =================================================

            var contentsArray =
                    objectMapper.createArrayNode();

            var contentObject =
                    objectMapper.createObjectNode();


            // =================================================
            // Parts
            // =================================================

            var partsArray =
                    objectMapper.createArrayNode();


            // =================================================
            // Image
            // =================================================

            var imagePart =
                    objectMapper.createObjectNode();

            var inlineData =
                    objectMapper.createObjectNode();

            inlineData.put(
                    "mimeType",
                    mediaType
            );

            inlineData.put(
                    "data",
                    base64Image
            );

            imagePart.set(
                    "inlineData",
                    inlineData
            );

            partsArray.add(imagePart);


            // =================================================
            // Prompt
            // =================================================

            var textPart =
                    objectMapper.createObjectNode();

            textPart.put(
                    "text",
                    prompt
            );

            partsArray.add(textPart);


            // =================================================
            // Build Contents
            // =================================================

            contentObject.set(
                    "parts",
                    partsArray
            );

            contentsArray.add(
                    contentObject
            );

            root.set(
                    "contents",
                    contentsArray
            );


            // =================================================
            // JSON Response Configuration
            // =================================================

            var generationConfig =
                    objectMapper.createObjectNode();

            generationConfig.put(
                    "responseMimeType",
                    "application/json"
            );

            root.set(
                    "generationConfig",
                    generationConfig
            );


            // =================================================
            // Gemini URL
            // =================================================

            String url =
                    GEMINI_URL +
                            geminiModel +
                            ":generateContent";


            // =================================================
            // HTTP Request
            // =================================================

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(url)
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "x-goog-api-key",
                                    geminiApiKey
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            root.toString(),
                                            StandardCharsets.UTF_8
                                    )
                            )
                            .build();


            // =================================================
            // Send Request
            // =================================================

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );


            // =================================================
            // Check Response
            // =================================================

            if (response.statusCode() != 200) {

                throw new CustomerException(
                        502,
                        "Gemini API error (" +
                                response.statusCode() +
                                "): " +
                                response.body()
                );
            }


            // =================================================
            // Parse Gemini Response
            // =================================================

            JsonNode responseJson =
                    objectMapper.readTree(
                            response.body()
                    );


            JsonNode candidates =
                    responseJson.path(
                            "candidates"
                    );


            if (!candidates.isArray() ||
                    candidates.isEmpty()) {

                throw new CustomerException(
                        502,
                        "Gemini returned no candidates"
                );
            }


            JsonNode parts =
                    candidates
                            .get(0)
                            .path("content")
                            .path("parts");


            if (!parts.isArray() ||
                    parts.isEmpty()) {

                throw new CustomerException(
                        502,
                        "Gemini returned no content"
                );
            }


            String rawText =
                    parts.get(0)
                            .path("text")
                            .asText("");


            if (rawText == null ||
                    rawText.isBlank()) {

                throw new CustomerException(
                        502,
                        "Gemini returned an empty response"
                );
            }


            // =================================================
            // Clean JSON
            // =================================================

            String cleaned =
                    rawText
                            .trim()
                            .replaceAll(
                                    "^```json\\s*",
                                    ""
                            )
                            .replaceAll(
                                    "^```\\s*",
                                    ""
                            )
                            .replaceAll(
                                    "\\s*```$",
                                    ""
                            )
                            .trim();


            // =================================================
            // Convert JSON String To JsonNode
            // =================================================

            return objectMapper.readTree(
                    cleaned
            );


        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new CustomerException(
                    502,
                    "Gemini API request was interrupted"
            );

        } catch (IOException e) {

            throw new CustomerException(
                    502,
                    "Failed to call Gemini API: " +
                            e.getMessage()
            );
        }
    }


    // =========================================================
    // SAVE INVOICE IMAGE
    // =========================================================

    private String saveInvoiceImage(
            MultipartFile file) {

        try {

            String uploadDir =
                    "uploads/invoices/";

            Path uploadPath =
                    Paths.get(uploadDir);


            if (!Files.exists(uploadPath)) {

                Files.createDirectories(
                        uploadPath
                );
            }


            String fileName =
                    UUID.randomUUID() +
                            "_" +
                            file.getOriginalFilename();


            Path filePath =
                    uploadPath.resolve(
                            fileName
                    );


            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );


            return "/uploads/invoices/" +
                    fileName;


        } catch (IOException e) {

            throw new CustomerException(
                    500,
                    "Failed to save invoice image: " +
                            e.getMessage()
            );
        }
    }


    // =========================================================
    // TEXT VALUE
    // =========================================================

    private String textOrNull(
            JsonNode node,
            String field) {

        JsonNode value =
                node.path(field);


        return (
                value.isMissingNode() ||
                        value.isNull()
        )
                ? null
                : value.asText(null);
    }


    // =========================================================
    // DATE VALUE
    // =========================================================

    private LocalDate dateOrNull(
            JsonNode node,
            String field) {

        String text =
                textOrNull(
                        node,
                        field
                );


        if (text == null ||
                text.isBlank()) {

            return null;
        }


        try {

            return LocalDate.parse(
                    text.substring(
                            0,
                            Math.min(
                                    10,
                                    text.length()
                            )
                    )
            );

        } catch (Exception e) {

            return null;
        }
    }


    // =========================================================
    // DECIMAL VALUE
    // =========================================================

    private BigDecimal decimalOrNull(
            JsonNode node,
            String field) {

        JsonNode value =
                node.path(field);


        if (value.isMissingNode() ||
                value.isNull()) {

            return null;
        }


        try {

            return new BigDecimal(
                    value.asText()
            );

        } catch (Exception e) {

            return null;
        }
    }


    // =========================================================
    // ITEM NAME SIMILARITY
    // =========================================================

    private double similarity(
            String a,
            String b) {

        String s1 =
                normalize(a);

        String s2 =
                normalize(b);


        if (s1.isEmpty() ||
                s2.isEmpty()) {

            return 0;
        }


        if (s1.equals(s2)) {

            return 1.0;
        }


        if (s1.contains(s2) ||
                s2.contains(s1)) {

            return 0.85;
        }


        int maxLen =
                Math.max(
                        s1.length(),
                        s2.length()
                );


        int distance =
                levenshtein(
                        s1,
                        s2
                );


        return 1.0 -
                ((double) distance /
                        maxLen);
    }


    // =========================================================
    // NORMALIZE
    // =========================================================

    private String normalize(
            String s) {

        return s == null
                ? ""
                : s.toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "[^a-z0-9]",
                        " "
                )
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }


    // =========================================================
    // LEVENSHTEIN DISTANCE
    // =========================================================

    private int levenshtein(
            String a,
            String b) {

        int[][] dp =
                new int[
                        a.length() + 1
                        ][
                        b.length() + 1
                        ];


        for (int i = 0;
             i <= a.length();
             i++) {

            dp[i][0] = i;
        }


        for (int j = 0;
             j <= b.length();
             j++) {

            dp[0][j] = j;
        }


        for (int i = 1;
             i <= a.length();
             i++) {

            for (int j = 1;
                 j <= b.length();
                 j++) {

                int cost =
                        a.charAt(i - 1)
                                ==
                                b.charAt(j - 1)
                                ? 0
                                : 1;


                dp[i][j] =
                        Math.min(
                                Math.min(
                                        dp[i - 1][j] + 1,
                                        dp[i][j - 1] + 1
                                ),
                                dp[i - 1][j - 1] +
                                        cost
                        );
            }
        }


        return dp[
                a.length()
                ][
                b.length()
                ];
    }


    // =========================================================
    // MAP ENTITY TO DTO
    // =========================================================

    private SupplierInvoiceDTO mapToDTO(
            SupplierInvoice invoice) {

        List<SupplierInvoiceItemDTO> itemDTOs =
                new ArrayList<>();


        for (SupplierInvoiceItem line :
                invoice.getItems()) {

            itemDTOs.add(
                    SupplierInvoiceItemDTO
                            .builder()
                            .invoiceItemId(
                                    line.getInvoiceItemId()
                            )
                            .extractedItemName(
                                    line.getExtractedItemName()
                            )
                            .quantity(
                                    line.getQuantity()
                            )
                            .unitPrice(
                                    line.getUnitPrice()
                            )
                            .matchedItemId(
                                    line.getMatchedItem() != null
                                            ?
                                            line.getMatchedItem()
                                                    .getItemId()
                                            :
                                            null
                            )
                            .matchedItemName(
                                    line.getMatchedItem() != null
                                            ?
                                            line.getMatchedItem()
                                                    .getItemName()
                                            :
                                            null
                            )
                            .matchConfidence(
                                    line.getMatchConfidence()
                            )
                            .build()
            );
        }


        return SupplierInvoiceDTO
                .builder()
                .invoiceId(
                        invoice.getInvoiceId()
                )
                .supplierName(
                        invoice.getSupplierName()
                )
                .invoiceDate(
                        invoice.getInvoiceDate()
                )
                .imageUrl(
                        invoice.getImageUrl()
                )
                .status(
                        invoice.getStatus()
                )
                .totalAmount(
                        invoice.getTotalAmount()
                )
                .items(
                        itemDTOs
                )
                .build();
    }
}