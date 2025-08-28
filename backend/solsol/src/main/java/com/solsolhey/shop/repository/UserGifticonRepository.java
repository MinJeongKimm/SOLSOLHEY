package com.solsolhey.shop.repository;

import com.solsolhey.shop.domain.UserGifticon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGifticonRepository extends JpaRepository<UserGifticon, Long> {

    List<UserGifticon> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserGifticon> findByIdAndUserId(Long id, Long userId);
}

