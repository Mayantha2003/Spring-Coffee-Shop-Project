package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.dto.ItemDTO;
import com.example.Spring_Coffee_Shop_Project.enumeration.ItemStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {

    void saveItem(ItemDTO itemDto);

    void updateItem(ItemDTO itemDto);

    void deleteItem(long id);

    ItemDTO getItemById(long id);

    List<ItemDTO> getAllItems();

    List<ItemDTO> getItemsByCategory(long categoryId);

    List<ItemDTO> getItemsByStatus(ItemStatus status);

    String saveImage(MultipartFile file);
}
