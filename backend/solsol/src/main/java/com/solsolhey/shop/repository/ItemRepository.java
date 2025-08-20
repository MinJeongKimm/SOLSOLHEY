package com.solsolhey.shop.repository;

import com.solsolhey.shop.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    List<Item> findByTypeAndIsActiveTrue(Item.ItemType type);
    
    List<Item> findByIsActiveTrue();
    
    boolean existsByIdAndIsActiveTrue(Long id);
}

