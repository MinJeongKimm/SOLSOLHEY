package com.solsolhey.shop.repository;

import com.solsolhey.shop.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Order.OrderStatus status);
}

