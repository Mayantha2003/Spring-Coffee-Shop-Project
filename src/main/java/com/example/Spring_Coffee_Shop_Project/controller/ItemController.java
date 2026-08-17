package com.example.Spring_Coffee_Shop_Project.controller;

import com.example.Spring_Coffee_Shop_Project.constant.CommonResponse;
import com.example.Spring_Coffee_Shop_Project.dto.ItemDTO;
import com.example.Spring_Coffee_Shop_Project.enumeration.ItemStatus;
import com.example.Spring_Coffee_Shop_Project.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<CommonResponse> saveItem (@RequestBody ItemDTO itemDTO){
        itemService.saveItem(itemDTO);
        return ResponseEntity.ok(new CommonResponse(200,"Item Saved Successfully"));
    }

    @PutMapping
    public ResponseEntity<CommonResponse> updateItem(@RequestBody ItemDTO itemDTO){
        itemService.updateItem(itemDTO);
        return ResponseEntity.ok(new CommonResponse(200,"Item Updated Successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse> deleteItem(@PathVariable Long id){
        itemService.deleteItem(id);
        return ResponseEntity.ok(new CommonResponse(200,"Item status updated to DISCONTINUED successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id){
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems(){
        return ResponseEntity.ok(itemService.getAllItems());
    }

    // Get Items By Category ID
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ItemDTO>> getItemsByCategory(@PathVariable long categoryId) {
        return ResponseEntity.ok(itemService.getItemsByCategory(categoryId));
    }

    // Get Items By Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ItemDTO>> getItemsByStatus(@PathVariable ItemStatus status) {
        return ResponseEntity.ok(itemService.getItemsByStatus(status));
    }

    // Frontend ItemStatuses Endpoint
    @GetMapping("/statuses")
    public ResponseEntity<ItemStatus[]> getItemStatuses() {
        return ResponseEntity.ok(ItemStatus.values());
    }

    // Upload image
    @PostMapping("/upload-image")
    public ResponseEntity<CommonResponse> uploadImage(@RequestParam("file") MultipartFile file){
        String imageUrl = itemService.saveImage(file);
        return ResponseEntity.ok(new CommonResponse(200, imageUrl));
    }
}
