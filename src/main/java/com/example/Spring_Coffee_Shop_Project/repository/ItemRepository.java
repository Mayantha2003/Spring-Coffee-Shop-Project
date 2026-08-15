package com.example.Spring_Coffee_Shop_Project.repository;

import com.example.Spring_Coffee_Shop_Project.entity.Item;
import com.example.Spring_Coffee_Shop_Project.enumeration.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item,Long> {

    List<Item> findByCategoryCategoryId(long categoryId);

    List<Item> findByItemStatus(ItemStatus status);
}
