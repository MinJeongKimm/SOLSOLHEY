package com.solsolhey.shop.repository;

import com.solsolhey.shop.domain.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    
    List<UserItem> findByUserId(Long userId);
    
    Optional<UserItem> findByUserIdAndItemId(Long userId, Long itemId);
    
    boolean existsByUserIdAndItemId(Long userId, Long itemId);
}

