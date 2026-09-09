package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.ItemDTO;
import com.example.Spring_Coffee_Shop_Project.entity.Category;
import com.example.Spring_Coffee_Shop_Project.entity.Item;
import com.example.Spring_Coffee_Shop_Project.enumeration.ItemStatus;
import com.example.Spring_Coffee_Shop_Project.exception.CustomerException;
import com.example.Spring_Coffee_Shop_Project.repository.CategoryRepository;
import com.example.Spring_Coffee_Shop_Project.repository.ItemRepository;
import com.example.Spring_Coffee_Shop_Project.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void saveItem(ItemDTO itemDto) {

        log.info("Executing Save Item method...");

        Optional<Category> optionalCategory = categoryRepository.findById(itemDto.getCategoryId());
        if (optionalCategory.isEmpty()) {
            throw new CustomerException(404, "Category not found with id: " + itemDto.getCategoryId());
        }

        Item item = mapToEntity(itemDto, optionalCategory.get());
        Item savedItem = itemRepository.save(item);

        log.info("Item saved successfully with id: {}", savedItem.getItemId());
    }

    @Override
    public void updateItem(ItemDTO itemDto) {

        log.info("Executing Full Update for Item ID: {}", itemDto.getItemId());

        Optional<Item> optionalItem = itemRepository.findById(itemDto.getItemId());
        if (optionalItem.isEmpty()) {
            throw new CustomerException(404, "Item not found with id: " + itemDto.getItemId());
        }

        Optional<Category> optionalCategory = categoryRepository.findById(itemDto.getCategoryId());
        if (optionalCategory.isEmpty()) {
            throw new CustomerException(404, "Category not found with id: " + itemDto.getCategoryId());
        }

        Item item = optionalItem.get();

        item.setItemName(itemDto.getItemName());
        item.setDescription(itemDto.getDescription());
        item.setPrice(itemDto.getPrice());
        item.setCostPrice(itemDto.getCostPrice());
        item.setDiscountPrice(itemDto.getDiscountPrice());
        item.setImageUrl(itemDto.getImageUrl());
        item.setItemStatus(itemDto.getItemStatus() != null ? itemDto.getItemStatus() : ItemStatus.AVAILABLE);
        item.setVeg(itemDto.isVeg());
        item.setPrepTimeMinutes(itemDto.getPrepTimeMinutes());
        item.setPoints(itemDto.getPoints());
        item.setCategory(optionalCategory.get());

        itemRepository.save(item);

        log.info("Item updated successfully with id: {}", item.getItemId());
    }
    @Override
    public void deleteItem(long id) {

        log.info("Executing Soft Delete for Item ID: {}", id);

        Optional<Item> optionalItem = itemRepository.findById(id);
        if (optionalItem.isEmpty()) {
            throw new CustomerException(404, "Item not found with id: " + id);
        }

        Item item = optionalItem.get();
        item.setItemStatus(ItemStatus.DISCONTINUED);
        itemRepository.save(item);

        log.info("Item marked as DISCONTINUED for id: {}", id);
    }

    @Override
    public ItemDTO getItemById(long id) {

        log.info("Fetching item by ID: {}", id);

        Optional<Item> optionalItem = itemRepository.findById(id);
        if (optionalItem.isEmpty()) {
            throw new CustomerException(404, "Item not found with id: " + id);
        }

        return mapToDTO(optionalItem.get());
    }

    @Override
    public List<ItemDTO> getAllItems() {

        log.info("Fetching all items...");

        List<Item> items = itemRepository.findAll();
        List<ItemDTO> itemDTOList = new ArrayList<>();

        for (Item item : items) {
            itemDTOList.add(mapToDTO(item));
        }

        return itemDTOList;
    }

    @Override
    public List<ItemDTO> getItemsByCategory(long categoryId) {

        log.info("Fetching items by Category ID: {}", categoryId);

        List<Item> items = itemRepository.findByCategoryCategoryId(categoryId);
        List<ItemDTO> itemDTOList = new ArrayList<>();

        for (Item item : items) {
            itemDTOList.add(mapToDTO(item));
        }

        return itemDTOList;
    }

    @Override
    public List<ItemDTO> getItemsByStatus(ItemStatus status) {

        log.info("Fetching items by Status: {}", status);

        List<Item> items = itemRepository.findByItemStatus(status);
        List<ItemDTO> itemDTOList = new ArrayList<>();

        for (Item item : items) {
            itemDTOList.add(mapToDTO(item));
        }

        return itemDTOList;
    }

    // Save Image
    @Override
    public String saveImage(MultipartFile file) {
        try {
            String uploadDir = "uploads/images/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/images/" + fileName;
        } catch (IOException e) {
            throw new CustomerException(500, "Failed to save image: " + e.getMessage());
        }
    }

    private ItemDTO mapToDTO(Item item) {
        return ItemDTO.builder()
                .itemId(item.getItemId())
                .itemName(item.getItemName())
                .description(item.getDescription())
                .price(item.getPrice())
                .costPrice(item.getCostPrice())
                .discountPrice(item.getDiscountPrice())
                .imageUrl(item.getImageUrl())
                .itemStatus(item.getItemStatus())
                .isVeg(item.isVeg())
                .prepTimeMinutes(item.getPrepTimeMinutes())
                .points(item.getPoints())
                .categoryId(item.getCategory() != null ? item.getCategory().getCategoryId() : 0)
                .build();
    }

    private Item mapToEntity(ItemDTO dto, Category category) {
        Item item = new Item();
        item.setItemId(dto.getItemId());
        item.setItemName(dto.getItemName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setCostPrice(dto.getCostPrice());
        item.setDiscountPrice(dto.getDiscountPrice());
        item.setImageUrl(dto.getImageUrl());
        item.setItemStatus(dto.getItemStatus() != null ? dto.getItemStatus() : ItemStatus.AVAILABLE);
        item.setVeg(dto.isVeg());
        item.setPrepTimeMinutes(dto.getPrepTimeMinutes());
        item.setPoints(dto.getPoints());
        item.setCategory(category);
        return item;
    }
}